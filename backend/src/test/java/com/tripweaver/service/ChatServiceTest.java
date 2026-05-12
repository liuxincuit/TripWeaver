package com.tripweaver.service;

import com.tripweaver.ai.AiService;
import com.tripweaver.entity.Conversation;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.ConversationRepository;
import com.tripweaver.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private Conversation existingConversation;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        existingConversation = new Conversation();
        existingConversation.setId(1L);
        existingConversation.setUserId(1L);
        existingConversation.setPlanId(100L);
        existingConversation.setMessages("[]");
    }

    @Test
    void sendMessage_shouldReturnResponse_whenConversationExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(conversationRepository.findByPlanIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingConversation));
        when(aiService.chat("你好", "100")).thenReturn("你好！我是旅行规划助手");

        String response = chatService.sendMessage(100L, "你好");

        assertEquals("你好！我是旅行规划助手", response);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldCreateConversation_whenNotExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(conversationRepository.findByPlanIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiService.chat("你好", "100")).thenReturn("你好！我是旅行规划助手");

        String response = chatService.sendMessage(100L, "你好");

        assertEquals("你好！我是旅行规划助手", response);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void createNewPlan_shouldReturnPlanId() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.save(any(TravelPlan.class))).thenAnswer(inv -> {
            TravelPlan plan = inv.getArgument(0);
            plan.setId(100L);
            return plan;
        });
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        Long planId = chatService.createNewPlan();

        assertEquals(100L, planId);
        verify(planRepository).save(any());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void getConversation_shouldReturnConversation_whenExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(conversationRepository.findByPlanIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(existingConversation));

        Optional<Conversation> result = chatService.getConversation(100L);

        assertTrue(result.isPresent());
        assertEquals(existingConversation, result.get());
    }

    @Test
    void getConversation_shouldReturnEmpty_whenNotExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(conversationRepository.findByPlanIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        Optional<Conversation> result = chatService.getConversation(100L);

        assertFalse(result.isPresent());
    }
}
