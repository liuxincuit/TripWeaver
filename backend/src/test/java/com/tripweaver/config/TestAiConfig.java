package com.tripweaver.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import com.tripweaver.chat.memory.TokenWindowChatMemory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestAiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return TokenWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxTokens(100000)
                .maxMessages(20)
                .build();
    }
}
