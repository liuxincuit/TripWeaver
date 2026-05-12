package com.tripweaver.service;

import com.tripweaver.ai.AiService;
import com.tripweaver.dto.ChatMessageDto;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserService userService;

    @Mock
    private ChatMemory chatMemory;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private TravelPlan testPlan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testPlan = new TravelPlan();
        testPlan.setId(100L);
        testPlan.setUserId(1L);
    }

    @Test
    void sendMessage_shouldReturnResponse() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testPlan));
        when(aiService.chat("你好", "100")).thenReturn("你好！我是旅行规划助手");

        String response = chatService.sendMessage(100L, "你好");

        assertEquals("你好！我是旅行规划助手", response);
    }

    @Test
    void sendMessage_shouldThrowAccessDeniedException_whenPlanNotOwnedByUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> chatService.sendMessage(100L, "你好"));
        verify(aiService, never()).chat(any(), any());
    }

    @Test
    void createNewPlan_shouldReturnPlanId() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.save(any(TravelPlan.class))).thenAnswer(inv -> {
            TravelPlan plan = inv.getArgument(0);
            plan.setId(100L);
            return plan;
        });

        Long planId = chatService.createNewPlan();

        assertEquals(100L, planId);
        verify(planRepository).save(any());
    }

    @Test
    void getHistory_shouldReturnMessages_whenHistoryExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testPlan));
        List<Message> messages = List.of(
                new UserMessage("你好"),
                new AssistantMessage("你好！我是旅行规划助手")
        );
        when(chatMemory.get("100")).thenReturn(messages);

        List<ChatMessageDto> result = chatService.getHistory(100L);

        assertEquals(2, result.size());
        assertEquals("user", result.get(0).role());
        assertEquals("你好", result.get(0).content());
        assertEquals("assistant", result.get(1).role());
        assertEquals("你好！我是旅行规划助手", result.get(1).content());
    }

    @Test
    void getHistory_shouldReturnEmptyList_whenNoHistory() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(testPlan));
        when(chatMemory.get("100")).thenReturn(List.of());

        List<ChatMessageDto> result = chatService.getHistory(100L);

        assertEquals(0, result.size());
    }

    @Test
    void getHistory_shouldThrowAccessDeniedException_whenPlanNotOwnedByUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> chatService.getHistory(100L));
        verify(chatMemory, never()).get(any());
    }
}
