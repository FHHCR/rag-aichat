package com.rag.aichat.dto;

import com.rag.aichat.entity.User;

public class UserInfoResponse {

    private String id;
    private String username;
    private String displayName;
    private String role;

    public static UserInfoResponse from(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.displayName = user.getDisplayName();
        response.role = user.getRole().name();
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
}
