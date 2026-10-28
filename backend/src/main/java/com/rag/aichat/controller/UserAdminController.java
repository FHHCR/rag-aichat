package com.rag.aichat.controller;

import com.rag.aichat.common.ApiResponse;
import com.rag.aichat.dto.UpdateUserRequest;
import com.rag.aichat.dto.UserInfoResponse;
import com.rag.aichat.security.CurrentUser;
import com.rag.aichat.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ApiResponse<List<UserInfoResponse>> list() {
        return ApiResponse.ok(userAdminService.list());
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> get(@PathVariable String userId) {
        return ApiResponse.ok(userAdminService.get(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> update(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userAdminService.update(CurrentUser.currentUserId(), userId, request)));
    }
}
