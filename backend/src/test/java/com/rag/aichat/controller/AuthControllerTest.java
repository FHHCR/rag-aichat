package com.rag.aichat.controller;

import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.entity.UserStatus;
import com.rag.aichat.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("infrastructure")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userMapper.deleteAll();
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setDisplayName("Administrator");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        userMapper.insert(user);
    }

    @Test
    void loginAndMeWork() throws Exception {
        String loginBody = """
                {
                  "username": "admin",
                  "password": "admin123"
                }
                """;

        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();

        String jwt = extractToken(token);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void registerCreatesUserAndReturnsToken() throws Exception {
        String registerBody = """
                {
                  "username": "demo_user_001",
                  "password": "password123",
                  "confirmPassword": "password123",
                  "displayName": "Demo User"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("demo_user_001"))
                .andExpect(jsonPath("$.data.user.displayName").value("Demo User"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        String jwt = extractToken(response);
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("demo_user_001"));
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        String registerBody = """
                {
                  "username": "admin",
                  "password": "password123",
                  "confirmPassword": "password123",
                  "displayName": "Duplicate"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("username already exists"));
    }

    private String extractToken(String responseBody) {
        int start = responseBody.indexOf("\"token\":\"");
        String remaining = responseBody.substring(start + "\"token\":\"".length());
        int end = remaining.indexOf("\"");
        return remaining.substring(0, end);
    }
}
