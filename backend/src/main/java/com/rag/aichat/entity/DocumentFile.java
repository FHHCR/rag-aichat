package com.rag.aichat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_files")
public class DocumentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "knowledge_base_id", nullable = false)
    private String knowledgeBaseId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_path", nullable = false)
    private String originalPath;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String parser;

    @Column(nullable = false)
    private String status;

    @Column(name = "parse_error", length = 2000)
    private String parseError;

    @Column(name = "chunk_count", nullable = false)
    private Integer chunkCount;

    @Column(name = "uploaded_by_id")
    private String uploadedById;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
