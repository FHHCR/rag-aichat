package com.rag.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateKnowledgeBaseRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be 120 chars or less")
    private String name;

    @Size(max = 500, message = "description must be 500 chars or less")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
