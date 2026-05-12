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
        userService.getCurrentUser();
        return aiService.chat(message, planId.toString());
    }

    public Long createNewPlan() {
        User user = userService.getCurrentUser();

        TravelPlan plan = new TravelPlan();
        plan.setUserId(user.getId());
        plan.setTitle("新旅行计划");

        TravelPlan savedPlan = planRepository.save(plan);
        return savedPlan.getId();
    }

    public List<ChatMessageDto> getHistory(Long planId) {
        userService.getCurrentUser();
        List<Message> messages = chatMemory.get(planId.toString());
        return messages.stream()
                .filter(m -> m instanceof UserMessage || m instanceof AssistantMessage)
                .map(m -> new ChatMessageDto(
                        m instanceof UserMessage ? "user" : "assistant",
                        m.getText()
                ))
                .toList();
    }
}
