package com.rag.aichat.repository;

import com.rag.aichat.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, String> {
}
