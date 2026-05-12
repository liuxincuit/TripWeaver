package com.tripweaver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripweaver.ai.AiService;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.PlanRepository;
import com.tripweaver.repository.UserRepository;
import com.tripweaver.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AiService aiService;

    private String token;
    private User testUser;
    private TravelPlan testPlan;

    @BeforeEach
    void setUp() {
        token = "test-token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");

        org.springframework.security.core.userdetails.User userDetails =
            new org.springframework.security.core.userdetails.User(
                "testuser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser = userRepository.save(testUser);

        testPlan = new TravelPlan();
        testPlan.setUserId(testUser.getId());
        testPlan.setTitle("测试计划");
        testPlan = planRepository.save(testPlan);

        when(aiService.chat(anyString(), anyString())).thenReturn("AI 响应");
    }

    @Test
    void sendMessage_shouldReturnResponse() throws Exception {
        Map<String, Object> request = Map.of("planId", 1, "message", "你好");
        mockMvc.perform(post("/api/chat/send")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response").exists());
    }

    @Test
    void sendMessage_shouldRejectUnauthorized() throws Exception {
        Map<String, Object> request = Map.of("planId", 1, "message", "你好");
        mockMvc.perform(post("/api/chat/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void sendMessage_shouldFailWithMissingPlanId() throws Exception {
        Map<String, Object> request = Map.of("message", "你好");
        mockMvc.perform(post("/api/chat/send")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("planId: planId 不能为空"));
    }

    @Test
    void sendMessage_shouldFailWithMissingMessage() throws Exception {
        Map<String, Object> request = Map.of("planId", 1);
        mockMvc.perform(post("/api/chat/send")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("message: message 不能为空"));
    }

    @Test
    void createNewPlan_shouldReturnPlanId() throws Exception {
        mockMvc.perform(post("/api/chat/new")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planId").exists());
    }

    @Test
    void createNewPlan_shouldRejectUnauthorized() throws Exception {
        mockMvc.perform(post("/api/chat/new"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getHistory_shouldReturnMessages_whenHistoryExists() throws Exception {
        mockMvc.perform(get("/api/chat/history/" + testPlan.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getHistory_shouldReturnEmptyList_whenNoHistory() throws Exception {
        mockMvc.perform(get("/api/chat/history/" + testPlan.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getHistory_shouldRejectUnauthorized() throws Exception {
        mockMvc.perform(get("/api/chat/history/1"))
            .andExpect(status().isForbidden());
    }
}