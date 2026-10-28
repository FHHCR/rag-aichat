package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.AuthLoginRequest;
import com.rag.aichat.dto.AuthLoginResponse;
import com.rag.aichat.dto.AuthRegisterRequest;
import com.rag.aichat.dto.UserInfoResponse;
import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserStatus;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.mapper.UserMapper;
import com.rag.aichat.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), 401, "invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), 401, "invalid credentials");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), 403, "user disabled");
        }
        return new AuthLoginResponse(jwtTokenProvider.createToken(user), UserInfoResponse.from(user));
    }

    public AuthLoginResponse register(AuthRegisterRequest request) {
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), 400, "password mismatch");
        }
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (userMapper.countByUsername(username) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), 409, "username already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() == null || request.getDisplayName().trim().isEmpty()
                ? username
                : request.getDisplayName().trim());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        userMapper.insert(user);
        User saved = user;
        return new AuthLoginResponse(jwtTokenProvider.createToken(saved), UserInfoResponse.from(saved));
    }

    public UserInfoResponse currentUser(User user) {
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), 403, "user disabled");
        }
        return UserInfoResponse.from(user);
    }
}
