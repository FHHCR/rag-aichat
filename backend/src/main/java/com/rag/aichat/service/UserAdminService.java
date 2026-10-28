package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.UpdateUserRequest;
import com.rag.aichat.dto.UserInfoResponse;
import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.entity.UserStatus;
import com.rag.aichat.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserInfoResponse> list() {
        return userMapper.findAll().stream().map(UserInfoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse get(String userId) {
        return UserInfoResponse.from(findUser(userId));
    }

    @Transactional
    public UserInfoResponse update(String actorId, String userId, UpdateUserRequest request) {
        User user = findUser(userId);
        if (request.getPassword() != null && !request.getPassword().isBlank() && request.getPassword().length() < 8) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "password must be at least 8 characters");
        }
        UserRole newRole = parseRole(user.getRole(), request.getRole());
        UserStatus newStatus = parseStatus(user.getStatus(), request.getStatus());

        if (isLastActiveAdmin(user) && (newRole != UserRole.ADMIN || newStatus != UserStatus.ACTIVE)) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), 409, "cannot disable or demote the last active admin");
        }
        if (user.getRole() != newRole && !request.getPassword().isBlank() && user.getId().equals(actorId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "cannot change own role through password update");
        }

        if (request.getDisplayName() != null) {
            String displayName = request.getDisplayName().trim();
            if (displayName.isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "displayName is required");
            }
            user.setDisplayName(displayName);
        }
        if (request.getRole() != null) user.setRole(newRole);
        if (request.getStatus() != null) user.setStatus(newStatus);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.update(user);
        User refreshed = userMapper.findById(user.getId());
        return UserInfoResponse.from(refreshed == null ? user : refreshed);
    }

    private User findUser(String userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), 404, "user not found");
        }
        return user;
    }

    private UserRole parseRole(UserRole current, String value) {
        if (value == null) return current;
        if ("ADMIN".equals(value)) return UserRole.ADMIN;
        if ("USER".equals(value)) return UserRole.USER;
        throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "invalid role");
    }

    private UserStatus parseStatus(UserStatus current, String value) {
        if (value == null) return current;
        if ("ACTIVE".equals(value)) return UserStatus.ACTIVE;
        if ("DISABLED".equals(value)) return UserStatus.DISABLED;
        throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "invalid status");
    }

    private boolean isLastActiveAdmin(User user) {
        return user.getRole() == UserRole.ADMIN && user.getStatus() == UserStatus.ACTIVE && userMapper.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE) == 1;
    }
}

