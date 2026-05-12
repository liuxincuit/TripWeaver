package com.tripweaver.service;

import com.tripweaver.ai.AiService;
import com.tripweaver.dto.ChatMessageDto;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiService aiService;
    private final PlanRepository planRepository;
    private final UserService userService;
    private final ChatMemory chatMemory;

    public String sendMessage(Long planId, String message) {
        validatePlanOwnership(planId);
        return aiService.chat(message, planId.toString());
    }

    private static final String WELCOME_MESSAGE = """
        你好！我是 TripWeaver 旅行规划助手。🌍

        请告诉我你的旅行想法：想去哪里？什么时候出发？有什么特别的偏好？我会为你量身定制一份完美的旅行计划。
        """;

    public Long createNewPlan() {
        User user = userService.getCurrentUser();

        TravelPlan plan = new TravelPlan();
        plan.setUserId(user.getId());
        plan.setTitle("新旅行计划");

        TravelPlan savedPlan = planRepository.save(plan);

        // 存储欢迎消息到 ChatMemory
        chatMemory.add(savedPlan.getId().toString(), List.of(new AssistantMessage(WELCOME_MESSAGE)));

        return savedPlan.getId();
    }

    public List<ChatMessageDto> getHistory(Long planId) {
        validatePlanOwnership(planId);
        List<Message> messages = chatMemory.get(planId.toString());
        return messages.stream()
                .filter(m -> m instanceof UserMessage || m instanceof AssistantMessage)
                .map(m -> new ChatMessageDto(
                        m instanceof UserMessage ? "user" : "assistant",
                        m.getText()
                ))
                .toList();
    }

    private void validatePlanOwnership(Long planId) {
        User user = userService.getCurrentUser();
        planRepository.findByIdAndUserId(planId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("无权访问此计划"));
    }
}
