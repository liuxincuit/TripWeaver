package com.tripweaver.service;

import com.tripweaver.ai.AiService;
import com.tripweaver.entity.Conversation;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.ConversationRepository;
import com.tripweaver.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiService aiService;
    private final ConversationRepository conversationRepository;
    private final PlanRepository planRepository;
    private final UserService userService;

    public String sendMessage(Long planId, String message) {
        User user = userService.getCurrentUser();

        // 获取或创建对话
        Conversation conversation = conversationRepository.findByPlanIdAndUserId(planId, user.getId())
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setUserId(user.getId());
                    newConv.setPlanId(planId);
                    newConv.setMessages("[]");
                    return conversationRepository.save(newConv);
                });

        // 调用 AI 服务
        String history = conversation.getMessages();
        String response = aiService.chat(message, history);

        // 更新对话历史
        String updatedMessages = updateConversationHistory(history, message, response);
        conversation.setMessages(updatedMessages);
        conversationRepository.save(conversation);

        return response;
    }

    public Long createNewPlan() {
        User user = userService.getCurrentUser();

        TravelPlan plan = new TravelPlan();
        plan.setUserId(user.getId());
        plan.setTitle("新旅行计划");

        TravelPlan savedPlan = planRepository.save(plan);

        // 创建对应的对话
        Conversation conversation = new Conversation();
        conversation.setUserId(user.getId());
        conversation.setPlanId(savedPlan.getId());
        conversation.setMessages("[]");
        conversationRepository.save(conversation);

        return savedPlan.getId();
    }

    public Optional<Conversation> getConversation(Long planId) {
        User user = userService.getCurrentUser();
        return conversationRepository.findByPlanIdAndUserId(planId, user.getId());
    }

    private String updateConversationHistory(String history, String userMessage, String assistantResponse) {
        // 简单实现：追加消息到历史记录
        // 实际项目中应该使用 JSON 数组
        return history.replace("]", String.format(
            ",{\"role\":\"user\",\"content\":\"%s\"},{\"role\":\"assistant\",\"content\":\"%s\"}]",
            escapeJson(userMessage), escapeJson(assistantResponse)
        ));
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n");
    }
}