package com.rag.aichat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.aichat.common.BusinessException;
import com.rag.aichat.config.RagProperties;
import com.rag.aichat.dto.AskRequest;
import com.rag.aichat.dto.ChatMessageResponse;
import com.rag.aichat.dto.ChatSessionResponse;
import com.rag.aichat.dto.CreateChatMessageRequest;
import com.rag.aichat.dto.CreateChatSessionRequest;
import com.rag.aichat.dto.DocumentChunkView;
import com.rag.aichat.entity.ChatMessage;
import com.rag.aichat.entity.ChatSession;
import com.rag.aichat.entity.KnowledgeBase;
import com.rag.aichat.mapper.ChatMessageMapper;
import com.rag.aichat.mapper.ChatSessionMapper;
import com.rag.aichat.mapper.KnowledgeBaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
public class ChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionService.class);
    private static final String DEFAULT_TITLE = "新会话";
    private static final Set<String> ALLOWED_ROLES = Set.of("user", "assistant");
    private static final long STREAM_TIMEOUT_MS = 5 * 60 * 1000L;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final OpenAiCompatibleClient openAiCompatibleClient;
    private final RagProperties ragProperties;
    private final VectorSearchService vectorSearchService;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ObjectMapper objectMapper;
    private final ExecutorService streamingExecutor;

    public ChatSessionService(ChatSessionMapper chatSessionMapper,
                              ChatMessageMapper chatMessageMapper,
                              OpenAiCompatibleClient openAiCompatibleClient,
                              RagProperties ragProperties,
                              VectorSearchService vectorSearchService,
                              KnowledgeBaseMapper knowledgeBaseMapper,
                              ObjectMapper objectMapper,
                              ExecutorService streamingExecutor) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.openAiCompatibleClient = openAiCompatibleClient;
        this.ragProperties = ragProperties;
        this.vectorSearchService = vectorSearchService;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.objectMapper = objectMapper;
        this.streamingExecutor = streamingExecutor;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(String userId) {
        return chatSessionMapper.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(userId).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public ChatSessionResponse createSession(CreateChatSessionRequest request, String userId) {
        String title = request.getTitle() == null || request.getTitle().isBlank() ? DEFAULT_TITLE : request.getTitle().trim();
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(title);
        session.setDeleted(false);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionMapper.insert(session);
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getSession(String sessionId, String userId) {
        return toSessionResponse(findOwnedSession(sessionId, userId));
    }

    @Transactional
    public void deleteSession(String sessionId, String userId) {
        ChatSession session = findOwnedSession(sessionId, userId);
        session.setDeleted(true);
        session.setDeletedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.update(session);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(String sessionId, String userId) {
        findOwnedSession(sessionId, userId);
        return chatMessageMapper.findBySessionIdAndUserIdOrderByCreatedAtAsc(sessionId, userId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse createMessage(String sessionId, CreateChatMessageRequest request, String userId) {
        ChatSession session = findOwnedSession(sessionId, userId);
        if (!ALLOWED_ROLES.contains(request.getRole())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "unsupported message role");
        }
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "content is required");
        }

        ChatMessage saved = saveMessage(session, userId, request.getRole(), content, null);
        return toMessageResponse(saved);
    }

    public SseEmitter askStream(String sessionId, AskRequest request, String userId) {
        ChatSession session = findOwnedSession(sessionId, userId);
        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "content is required");
        }
        saveMessage(session, userId, "user", content, null);

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        streamingExecutor.execute(() -> runStream(emitter, session, userId, content, request.getKnowledgeBaseIds()));
        return emitter;
    }

    private void runStream(SseEmitter emitter, ChatSession session, String userId, String content, List<String> requestedIds) {
        try {
            List<String> knowledgeBaseIds = normalizeKnowledgeBaseScope(session, requestedIds);
            List<DocumentChunkView> chunks;
            if (knowledgeBaseIds.isEmpty()) {
                chunks = List.of();
            } else {
                float[] questionVector = openAiCompatibleClient.embed(List.of(content)).get(0);
                chunks = vectorSearchService.search(questionVector, knowledgeBaseIds, ragProperties.getRag().getTopK());
            }

            StringBuilder answer = new StringBuilder();
            openAiCompatibleClient.streamChat(List.of(
                    Map.of("role", "system", "content", "You are Rag-AiChat. Answer using the provided sources when they are relevant and mention source numbers."),
                    Map.of("role", "user", "content", buildRagPrompt(content, chunks))
            ), chunk -> {
                answer.append(chunk);
                sendEvent(emitter, "chunk", Map.of("content", chunk));
            });

            ChatMessage assistantMessage = saveMessage(session, userId, "assistant", answer.toString(), serializeSources(chunks));
            sendEvent(emitter, "done", Map.of("message", toMessageResponse(assistantMessage)));
            emitter.complete();
        } catch (Exception e) {
            log.warn("Streaming ask failed for session {}", session.getId(), e);
            sendEvent(emitter, "error", Map.of("message", e.getMessage() == null ? "生成回答失败" : e.getMessage()));
            emitter.complete();
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException | IllegalStateException e) {
            log.debug("SSE client disconnected while sending {}", name);
        }
    }

    private ChatSession findOwnedSession(String sessionId, String userId) {
        ChatSession session = chatSessionMapper.findByIdAndUserIdAndDeletedFalse(sessionId, userId);
        if (session == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), 404, "chat session not found");
        }
        return session;
    }

    private ChatMessage saveMessage(ChatSession session, String userId, String role, String content, String sourceJson) {
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSessionId(session.getId());
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setSourceJson(sourceJson);
        message.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.update(session);
        chatMessageMapper.insert(message);
        return message;
    }

    private List<String> normalizeKnowledgeBaseScope(ChatSession session, List<String> requestedIds) {
        List<String> sourceIds = requestedIds == null || requestedIds.isEmpty()
                ? knowledgeBaseMapper.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                    .map(KnowledgeBase::getId)
                    .toList()
                : requestedIds;
        List<String> ids = sourceIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        for (String id : ids) {
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.findById(id);
            if (knowledgeBase == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "knowledge base not found");
            }
            if (!"ACTIVE".equals(knowledgeBase.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "knowledge base is not active");
            }
        }
        session.setKnowledgeBaseScope(writeKnowledgeBaseScope(ids));
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.update(session);
        return ids;
    }

    private String buildRagPrompt(String question, List<DocumentChunkView> chunks) {
        if (chunks.isEmpty()) {
            return "用户问题：" + question + "\n当前没有可用的知识库来源，请如实说明。";
        }
        StringBuilder sb = new StringBuilder("请使用以下来源回答用户问题；如果来源不足，请说明不足。\n\n");
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunkView chunk = chunks.get(i);
            sb.append("来源").append(i + 1).append("：").append(chunk.content()).append("\n\n");
        }
        sb.append("用户问题：").append(question);
        return sb.toString();
    }

    private String serializeSources(List<DocumentChunkView> chunks) {
        try {
            return objectMapper.writeValueAsString(chunks.stream().map(chunk -> Map.of(
                    "id", chunk.id(),
                    "documentFileId", chunk.documentFileId(),
                    "knowledgeBaseId", chunk.knowledgeBaseId(),
                    "chunkIndex", chunk.chunkIndex(),
                    "preview", chunk.content() == null ? "" : chunk.content()
            )).toList());
        } catch (JsonProcessingException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "source serialization failed");
        }
    }

    private List<String> parseKnowledgeBaseScope(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String writeKnowledgeBaseScope(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setKnowledgeBaseScope(session.getKnowledgeBaseScope());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setSourceJson(message.getSourceJson());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
