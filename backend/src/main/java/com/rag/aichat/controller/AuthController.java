package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.AuthLoginRequest;
import com.rag.aichat.dto.AuthLoginResponse;
import com.rag.aichat.dto.UserInfoResponse;
import com.rag.aichat.entity.User;
import com.rag.aichat.security.LoginUser;
import com.rag.aichat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(@AuthenticationPrincipal LoginUser loginUser) {
        User user = loginUser.getUser();
        return ApiResponse.ok(authService.currentUser(user));
    }
}
