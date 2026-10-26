package com.rag.aichat.repository;

import com.rag.aichat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findBySessionIdAndUserIdOrderByCreatedAtAsc(String sessionId, String userId);
}
