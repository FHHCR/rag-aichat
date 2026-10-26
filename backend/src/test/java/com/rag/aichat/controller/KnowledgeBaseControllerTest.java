package com.rag.aichat.controller;

import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.entity.UserStatus;
import com.rag.aichat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KnowledgeBaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminUsername;
    private String adminPassword;
    private String userUsername;
    private String userPassword;

    @BeforeEach
    void setUp() {
        adminUsername = "admin-kb-" + UUID.randomUUID();
        adminPassword = "Admin123456!";
        userUsername = "user-kb-" + UUID.randomUUID();
        userPassword = "User123456!";

        saveUser(adminUsername, adminPassword, UserRole.ADMIN);
        saveUser(userUsername, userPassword, UserRole.USER);
    }

    @Test
    void anonymousCannotCreateKnowledgeBase() throws Exception {
        mockMvc.perform(post("/api/admin/knowledge-bases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Secret KB\",\"description\":\"private\"}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message", containsString("access denied")));
    }

    @Test
    void userCannotCreateKnowledgeBase() throws Exception {
        String userToken = login(userUsername, userPassword);

        mockMvc.perform(post("/api/admin/knowledge-bases")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User KB\",\"description\":\"private\"}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message", containsString("access denied")));
    }

    @Test
    void adminCanCreateListUpdateAndDeleteKnowledgeBase() throws Exception {
        String adminToken = login(adminUsername, adminPassword);
        String createBody = "{\"name\":\"Policy KB\",\"description\":\"Policy documents\"}";

        String created = mockMvc.perform(post("/api/admin/knowledge-bases")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.message", is("ok")))
                .andExpect(jsonPath("$.data.name", is("Policy KB")))
                .andExpect(jsonPath("$.data.description", is("Policy documents")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andReturn().getResponse().getContentAsString();

        String id = extractDataField(created, "id");

        mockMvc.perform(get("/api/admin/knowledge-bases")
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data[?(@.name == 'Policy KB')].description", is(java.util.Collections.singletonList("Policy documents"))));

        mockMvc.perform(get("/api/admin/knowledge-bases/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(id)))
                .andExpect(jsonPath("$.data.name", is("Policy KB")));

        String updateBody = "{\"name\":\"Policy KB v2\",\"description\":\"Policy documents updated\"}";
        mockMvc.perform(put("/api/admin/knowledge-bases/{id}", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.name", is("Policy KB v2")))
                .andExpect(jsonPath("$.data.description", is("Policy documents updated")));

        mockMvc.perform(delete("/api/admin/knowledge-bases/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.message", is("ok")));

        mockMvc.perform(get("/api/admin/knowledge-bases/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(404)))
                .andExpect(jsonPath("$.message", is("knowledge base not found")));
    }

    private void saveUser(String username, String password, UserRole role) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(username);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private String login(String username, String password) throws Exception {
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
