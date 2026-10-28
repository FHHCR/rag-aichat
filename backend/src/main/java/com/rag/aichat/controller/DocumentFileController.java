package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.DocumentFileResponse;
import com.rag.aichat.security.CurrentUser;
import com.rag.aichat.service.DocumentFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge-bases/{knowledgeBaseId}/files")
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    public DocumentFileController(DocumentFileService documentFileService) {
        this.documentFileService = documentFileService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentFileResponse>> upload(
            @PathVariable String knowledgeBaseId,
            @RequestParam("file") MultipartFile file) {
        DocumentFileResponse response = documentFileService.upload(knowledgeBaseId, file, CurrentUser.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ApiResponse<List<DocumentFileResponse>> list(@PathVariable String knowledgeBaseId) {
        return ApiResponse.ok(documentFileService.list(knowledgeBaseId));
    }

    @GetMapping("/{fileId}")
    public ApiResponse<DocumentFileResponse> get(@PathVariable String knowledgeBaseId, @PathVariable String fileId) {
        return ApiResponse.ok(documentFileService.get(knowledgeBaseId, fileId));
    }

    @PostMapping("/{fileId}/reindex")
    public ApiResponse<DocumentFileResponse> reindex(
            @PathVariable String knowledgeBaseId,
            @PathVariable String fileId) {
        return ApiResponse.ok(documentFileService.reindex(knowledgeBaseId, fileId, CurrentUser.currentUserId()));
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> delete(@PathVariable String knowledgeBaseId, @PathVariable String fileId) {
        documentFileService.delete(knowledgeBaseId, fileId);
        return ApiResponse.ok(null);
    }
}
