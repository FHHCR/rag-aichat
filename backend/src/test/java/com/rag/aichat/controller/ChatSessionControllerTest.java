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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("infrastructure")
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userA;
    private String userB;
    private String password;

    @BeforeEach
    void setUp() {
        userA = "user-chat-a-" + UUID.randomUUID();
        userB = "user-chat-b-" + UUID.randomUUID();
        password = "User123456!";
        saveUser(userA, UserRole.USER);
        saveUser(userB, UserRole.USER);
    }

    @Test
    void anonymousCannotListSessions() throws Exception {
        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanCreateSessionAndOnlySeesOwnSession() throws Exception {
        String tokenA = login(userA);
        String tokenB = login(userB);

        mockMvc.perform(post("/api/chat/sessions")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.title", is("新会话")));

        String created = mockMvc.perform(post("/api/chat/sessions")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Session A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.title", is("Session A")))
                .andReturn().getResponse().getContentAsString();
        String sessionId = extractDataField(created, "id");

        mockMvc.perform(get("/api/chat/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == '" + sessionId + "')].title", is(java.util.Collections.singletonList("Session A"))));

        mockMvc.perform(get("/api/chat/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(404)))
                .andExpect(jsonPath("$.message", is("chat session not found")));

        mockMvc.perform(delete("/api/chat/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));

        mockMvc.perform(get("/api/chat/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    private void saveUser(String username, UserRole role) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(username);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userMapper.insert(user);
    }

    private String login(String username) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andReturn().getResponse().getContentAsString();
        return extractDataField(responseBody, "token");
    }

    private String extractDataField(String responseBody, String field) {
        String marker = "\"" + field + "\":\"";
        int start = responseBody.indexOf(marker);
        String remaining = responseBody.substring(start + marker.length());
        int end = remaining.indexOf("\"");
        return remaining.substring(0, end);
    }
}
