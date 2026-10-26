package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.CreateKnowledgeBaseRequest;
import com.rag.aichat.dto.KnowledgeBaseResponse;
import com.rag.aichat.dto.UpdateKnowledgeBaseRequest;
import com.rag.aichat.security.CurrentUser;
import com.rag.aichat.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeBaseResponse>> create(
            @Valid @RequestBody CreateKnowledgeBaseRequest request) {
        String createdById = CurrentUser.currentUserId();
        KnowledgeBaseResponse response = knowledgeBaseService.create(request, createdById);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseResponse>> list() {
        return ApiResponse.ok(knowledgeBaseService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> get(@PathVariable String id) {
        return ApiResponse.ok(knowledgeBaseService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateKnowledgeBaseRequest request) {
        return ApiResponse.ok(knowledgeBaseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        knowledgeBaseService.delete(id);
        return ApiResponse.ok(null);
    }
}
