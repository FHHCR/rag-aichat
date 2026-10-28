package com.rag.aichat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SystemStatusResponse {
    private String appVersion = "v1.0";
    private LocalDateTime timestamp;
    private List<ComponentStatus> components;

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public List<ComponentStatus> getComponents() { return components; }
    public void setComponents(List<ComponentStatus> components) { this.components = components; }

    public record ComponentStatus(String name, String status, String detail) {
    }
}

