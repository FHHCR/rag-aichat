package com.rag.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateChatMessageRequest {

    @NotBlank(message = "role is required")
    private String role;

    @NotBlank(message = "content is required")
    @Size(max = 4000, message = "content must be 4000 chars or less")
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
