package com.tripweaver.chat.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenWindowChatMemoryTest {

    private static final String CONVERSATION_ID = "test-conversation";

    private ChatMemory chatMemory;

    @BeforeEach
    void setUp() {
        chatMemory = TokenWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxTokens(100)
                .maxMessages(20)
                .build();
    }

    @Test
    void shouldKeepMessagesWithinTokenLimit() {
        List<Message> messages = List.of(
                new UserMessage("Hello world. Hello world. Hello world. Hello world."),
                new AssistantMessage("Hello world. Hello world. Hello world. Hello world."),
                new UserMessage("Hello world. Hello world. Hello world. Hello world."),
                new AssistantMessage("Hello world. Hello world. Hello world. Hello world."),
                new UserMessage("Hello world. Hello world. Hello world. Hello world."),
                new AssistantMessage("Hello world. Hello world. Hello world. Hello world.")
        );

        chatMemory.add(CONVERSATION_ID, messages);
        List<Message> stored = chatMemory.get(CONVERSATION_ID);

        assertThat(stored).hasSizeLessThan(messages.size());
        assertThat(stored).isNotEmpty();
    }

    @Test
    void shouldPreserveSystemMessages() {
        SystemMessage systemMsg = new SystemMessage("You are a helpful travel assistant.");
        chatMemory.add(CONVERSATION_ID, List.of(systemMsg));

        for (int i = 0; i < 20; i++) {
            chatMemory.add(CONVERSATION_ID, List.of(
                    new UserMessage("Message " + i + " with some extra text to increase token count. ".repeat(3)),
                    new AssistantMessage("Response " + i + " with some extra text to increase token count. ".repeat(3))
            ));
        }

        List<Message> stored = chatMemory.get(CONVERSATION_ID);
        assertThat(stored).anyMatch(m -> m instanceof SystemMessage);
    }

    @Test
    void shouldHandleEmptyMessages() {
        chatMemory.add(CONVERSATION_ID, List.of());
        List<Message> stored = chatMemory.get(CONVERSATION_ID);
        assertThat(stored).isEmpty();
    }

    @Test
    void shouldClearMessages() {
        chatMemory.add(CONVERSATION_ID, List.of(new UserMessage("Hello")));
        chatMemory.clear(CONVERSATION_ID);
        List<Message> stored = chatMemory.get(CONVERSATION_ID);
        assertThat(stored).isEmpty();
    }

    @Test
    void shouldHonorMaxMessagesLimit() {
        chatMemory = TokenWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxTokens(100000)
                .maxMessages(3)
                .build();

        for (int i = 0; i < 10; i++) {
            chatMemory.add(CONVERSATION_ID, List.of(new UserMessage("Message " + i)));
        }

        List<Message> stored = chatMemory.get(CONVERSATION_ID);
        assertThat(stored).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void shouldDedupSystemMessages() {
        SystemMessage sys1 = new SystemMessage("System prompt v1");
        SystemMessage sys2 = new SystemMessage("System prompt v2");

        chatMemory.add(CONVERSATION_ID, List.of(sys1));
        chatMemory.add(CONVERSATION_ID, List.of(sys2));

        List<Message> stored = chatMemory.get(CONVERSATION_ID);
        long systemCount = stored.stream().filter(m -> m instanceof SystemMessage).count();
        assertThat(systemCount).isEqualTo(1);
        assertThat(stored).contains(sys2);
    }
}
