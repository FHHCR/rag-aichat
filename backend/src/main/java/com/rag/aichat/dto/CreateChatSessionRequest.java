package com.rag.aichat.dto;

import jakarta.validation.constraints.Size;

public class CreateChatSessionRequest {

    @Size(max = 100, message = "title must be 100 chars or less")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
