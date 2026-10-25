package com.rag.aichat.repository;

import com.rag.aichat.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {
}
