package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.ChatMessageResponse;
import com.rag.aichat.dto.CreateChatMessageRequest;
import com.rag.aichat.security.CurrentUser;
import com.rag.aichat.service.ChatSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/sessions/{sessionId}/messages")
public class ChatMessageController {

    private final ChatSessionService chatSessionService;

    public ChatMessageController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @GetMapping
    public ApiResponse<List<ChatMessageResponse>> list(@PathVariable String sessionId) {
        return ApiResponse.ok(chatSessionService.listMessages(sessionId, CurrentUser.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> create(
            @PathVariable String sessionId,
            @Valid @RequestBody CreateChatMessageRequest request) {
        ChatMessageResponse response = chatSessionService.createMessage(sessionId, request, CurrentUser.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
