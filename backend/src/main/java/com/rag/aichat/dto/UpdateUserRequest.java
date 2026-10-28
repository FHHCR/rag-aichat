package com.rag.aichat.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    private String displayName;
    private String role;
    private String status;
    @Size(min = 0, max = 100)
    private String password;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

