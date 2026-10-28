package com.rag.aichat.dto;

import com.rag.aichat.entity.User;

import java.time.LocalDateTime;

public class UserInfoResponse {

    private String id;
    private String username;
    private String displayName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserInfoResponse from(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.displayName = user.getDisplayName();
        response.role = user.getRole().name();
        response.status = user.getStatus().name();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        return response;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
