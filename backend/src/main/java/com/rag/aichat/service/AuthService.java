package com.rag.aichat.service;

import com.rag.aichat.common.BusinessException;
import com.rag.aichat.dto.AuthLoginRequest;
import com.rag.aichat.dto.AuthLoginResponse;
import com.rag.aichat.dto.UserInfoResponse;
import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserStatus;
import com.rag.aichat.repository.UserRepository;
import com.rag.aichat.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED.value(), 401, "invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), 401, "invalid credentials");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), 403, "user disabled");
        }
        return new AuthLoginResponse(jwtTokenProvider.createToken(user), UserInfoResponse.from(user));
    }

    public UserInfoResponse currentUser(User user) {
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), 403, "user disabled");
        }
        return UserInfoResponse.from(user);
    }
}
