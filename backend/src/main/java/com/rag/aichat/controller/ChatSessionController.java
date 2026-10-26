package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.ChatSessionResponse;
import com.rag.aichat.dto.CreateChatSessionRequest;
import com.rag.aichat.security.CurrentUser;
import com.rag.aichat.service.ChatSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @GetMapping
    public ApiResponse<List<ChatSessionResponse>> list() {
        return ApiResponse.ok(chatSessionService.listSessions(CurrentUser.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatSessionResponse>> create(@Valid @RequestBody CreateChatSessionRequest request) {
        ChatSessionResponse response = chatSessionService.createSession(request, CurrentUser.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatSessionResponse> get(@PathVariable String id) {
        return ApiResponse.ok(chatSessionService.getSession(id, CurrentUser.currentUserId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        chatSessionService.deleteSession(id, CurrentUser.currentUserId());
        return ApiResponse.ok(null);
    }
}
