package com.rag.aichat.entity;

import java.time.LocalDateTime;

public class DocumentFile {

    public static final String STATUS_UPLOADED = "UPLOADED";
    public static final String STATUS_PARSING = "PARSING";
    public static final String STATUS_PARSED = "PARSED";
    public static final String STATUS_INDEXING = "INDEXING";
    public static final String STATUS_INDEXED = "INDEXED";
    public static final String STATUS_PARSED_FAILED = "PARSED_FAILED";
    public static final String STATUS_INDEX_FAILED = "INDEX_FAILED";

    private String id;

    private String knowledgeBaseId;

    private String fileName;

    private String originalPath;

    private String mimeType;

    private Long fileSize;

    private String parser;

    private String status;

    private String parseError;

    private Integer chunkCount;

    private String uploadedById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParseError() {
        return parseError;
    }

    public void setParseError(String parseError) {
        this.parseError = parseError;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(String uploadedById) {
        this.uploadedById = uploadedById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
