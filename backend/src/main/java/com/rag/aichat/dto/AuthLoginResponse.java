package com.rag.aichat.dto;

public class AuthLoginResponse {

    private String token;
    private UserInfoResponse user;

    public AuthLoginResponse() {
    }

    public AuthLoginResponse(String token, UserInfoResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoResponse getUser() {
        return user;
    }

    public void setUser(UserInfoResponse user) {
        this.user = user;
    }
}
