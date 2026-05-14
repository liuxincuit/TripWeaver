package com.tripweaver.config;

import com.tripweaver.chat.memory.TokenWindowChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository,
                                  @Value("${spring.ai.chat.memory.token-window.max-tokens:100000}") int maxTokens,
                                  @Value("${spring.ai.chat.memory.token-window.max-messages:20}") int maxMessages) {
        return TokenWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxTokens(maxTokens)
                .maxMessages(maxMessages)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
