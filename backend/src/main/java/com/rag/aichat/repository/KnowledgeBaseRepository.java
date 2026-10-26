package com.rag.aichat.repository;

import com.rag.aichat.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, String> {

    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();
}
