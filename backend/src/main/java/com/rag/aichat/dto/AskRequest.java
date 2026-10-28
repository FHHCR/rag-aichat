package com.rag.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AskRequest {
    @NotBlank
    private String content;
    private List<String> knowledgeBaseIds;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getKnowledgeBaseIds() { return knowledgeBaseIds; }
    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) { this.knowledgeBaseIds = knowledgeBaseIds; }
}

