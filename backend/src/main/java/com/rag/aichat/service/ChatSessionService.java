package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.ChatMessageResponse;
import com.rag.aichat.dto.ChatSessionResponse;
import com.rag.aichat.dto.CreateChatMessageRequest;
import com.rag.aichat.dto.CreateChatSessionRequest;
import com.rag.aichat.entity.ChatMessage;
import com.rag.aichat.entity.ChatSession;
import com.rag.aichat.repository.ChatMessageRepository;
import com.rag.aichat.repository.ChatSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ChatSessionService {

    private static final String DEFAULT_TITLE = "新会话";
    private static final Set<String> ALLOWED_ROLES = Set.of("user", "assistant");

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatSessionService(ChatSessionRepository chatSessionRepository, ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(String userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public ChatSessionResponse createSession(CreateChatSessionRequest request, String userId) {
        String title = request.getTitle() == null || request.getTitle().isBlank() ? DEFAULT_TITLE : request.getTitle().trim();
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        return toSessionResponse(chatSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse getSession(String sessionId, String userId) {
        return toSessionResponse(findOwnedSession(sessionId, userId));
    }

    @Transactional
    public void deleteSession(String sessionId, String userId) {
        ChatSession session = findOwnedSession(sessionId, userId);
        chatSessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(String sessionId, String userId) {
        findOwnedSession(sessionId, userId);
        return chatMessageRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(sessionId, userId).stream()
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

        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(userId);
        message.setRole(request.getRole());
        message.setContent(content);
        ChatMessage saved = chatMessageRepository.save(message);

        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        return toMessageResponse(saved);
    }

    private ChatSession findOwnedSession(String sessionId, String userId) {
        return chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), 404, "chat session not found"));
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
