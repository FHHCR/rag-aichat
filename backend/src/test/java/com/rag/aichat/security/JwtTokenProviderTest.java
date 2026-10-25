package com.rag.aichat.security;

import com.rag.aichat.config.SecurityProperties;
import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.entity.UserStatus;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.setSecret("unit-test-secret-for-jwt-issuer-must-be-long-enough-0123456789abcdef");
        properties.setExpirationMs(3600000);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void tokenCanRoundTrip() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        String token = jwtTokenProvider.createToken(user);
        Claims claims = jwtTokenProvider.parseToken(token);

        assertEquals("user-1", claims.getSubject());
        assertEquals("admin", claims.get("username"));
        assertEquals("ADMIN", claims.get("role"));
    }
}
