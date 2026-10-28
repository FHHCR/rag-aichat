package com.rag.aichat.dto;

import java.time.LocalDateTime;

public record DocumentChunkView(String id, String documentFileId, String knowledgeBaseId, int chunkIndex, String content, LocalDateTime createdAt) {
    public String preview() {
        String clean = content == null ? "" : content.replaceAll("\\s+", " ").trim();
        return clean.length() <= 220 ? clean : clean.substring(0, 220) + "…";
    }
}

