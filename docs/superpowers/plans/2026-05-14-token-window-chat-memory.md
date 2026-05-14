# TokenWindowChatMemory Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `MessageWindowChatMemory` with `TokenWindowChatMemory` that limits conversation history by token count (with message count as safety net).

**Architecture:** Implement `ChatMemory` wrapping `ChatMemoryRepository`, using Spring AI's built-in `JTokkitTokenCountEstimator` for token counting. On `add()`, merge messages, dedup SystemMessages, then trim oldest non-system messages until within both `maxTokens` and `maxMessages` limits.

**Tech Stack:** Java 21, Spring AI 1.1.6, JTokkit (bundled)

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `backend/src/main/java/com/tripweaver/chat/memory/TokenWindowChatMemory.java` | Create | Custom ChatMemory with token-based windowing |
| `backend/src/main/java/com/tripweaver/config/AiConfig.java` | Modify | Replace ChatMemory bean |
| `backend/src/test/java/com/tripweaver/config/TestAiConfig.java` | Modify | Replace test ChatMemory bean |
| `backend/src/main/resources/application.yml` | Modify | Add token-window config |
| `backend/src/test/java/com/tripweaver/chat/memory/TokenWindowChatMemoryTest.java` | Create | Unit tests |

---

### Task 1: Create TokenWindowChatMemoryTest (Red)

**Files:**
- Create: `backend/src/test/java/com/tripweaver/chat/memory/TokenWindowChatMemoryTest.java`

- [ ] **Step 1: Write the failing test**

```java
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
                .maxTokens(100) // small limit for testing
                .maxMessages(20)
                .build();
    }

    @Test
    void shouldKeepMessagesWithinTokenLimit() {
        // Each message ~25 tokens ("Hello world" repeated a few times)
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

        // Add many messages to trigger token-based trimming
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd backend && mvn test -Dtest=TokenWindowChatMemoryTest -q
```

Expected: COMPILE ERROR — `TokenWindowChatMemory` class does not exist.

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/tripweaver/chat/memory/TokenWindowChatMemoryTest.java
git commit -m "test: add TokenWindowChatMemory unit tests (red)"
```

---

### Task 2: Implement TokenWindowChatMemory (Green)

**Files:**
- Create: `backend/src/main/java/com/tripweaver/chat/memory/TokenWindowChatMemory.java`

- [ ] **Step 1: Write TokenWindowChatMemory implementation**

```java
package com.tripweaver.chat.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TokenWindowChatMemory implements ChatMemory {

    private static final int DEFAULT_MAX_TOKENS = 100000;
    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final ChatMemoryRepository chatMemoryRepository;
    private final int maxTokens;
    private final int maxMessages;
    private final TokenCountEstimator tokenEstimator;

    private TokenWindowChatMemory(ChatMemoryRepository chatMemoryRepository, int maxTokens, int maxMessages) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxTokens > 0, "maxTokens must be greater than 0");
        Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
        this.chatMemoryRepository = chatMemoryRepository;
        this.maxTokens = maxTokens;
        this.maxMessages = maxMessages;
        this.tokenEstimator = new JTokkitTokenCountEstimator();
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        List<Message> existing = chatMemoryRepository.findByConversationId(conversationId);
        List<Message> merged = merge(existing, messages);
        List<Message> trimmed = trim(merged);
        chatMemoryRepository.saveAll(conversationId, trimmed);
    }

    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        return chatMemoryRepository.findByConversationId(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        chatMemoryRepository.deleteByConversationId(conversationId);
    }

    private List<Message> merge(List<Message> existing, List<Message> newMessages) {
        List<Message> result = new ArrayList<>();
        Set<Message> existingSet = new HashSet<>(existing);

        boolean hasNewSystemMessage = newMessages.stream()
                .filter(m -> m instanceof SystemMessage)
                .anyMatch(m -> !existingSet.contains(m));

        for (Message msg : existing) {
            if (hasNewSystemMessage && msg instanceof SystemMessage) {
                continue;
            }
            result.add(msg);
        }

        result.addAll(newMessages);
        return result;
    }

    private List<Message> trim(List<Message> messages) {
        List<Message> result = new ArrayList<>(messages);

        // Trim by message count first
        while (result.size() > maxMessages) {
            removeOldestNonSystemMessage(result);
        }

        // Trim by token count
        while (countTokens(result) > maxTokens && result.size() > countSystemMessages(result)) {
            removeOldestNonSystemMessage(result);
        }

        return result;
    }

    private void removeOldestNonSystemMessage(List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            if (!(messages.get(i) instanceof SystemMessage)) {
                messages.remove(i);
                return;
            }
        }
    }

    private int countTokens(List<Message> messages) {
        int total = 0;
        for (Message msg : messages) {
            String text = msg.getText();
            if (text != null) {
                total += tokenEstimator.estimate(text);
            }
        }
        return total;
    }

    private long countSystemMessages(List<Message> messages) {
        return messages.stream().filter(m -> m instanceof SystemMessage).count();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ChatMemoryRepository chatMemoryRepository;
        private int maxTokens = DEFAULT_MAX_TOKENS;
        private int maxMessages = DEFAULT_MAX_MESSAGES;

        private Builder() {
        }

        public Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public TokenWindowChatMemory build() {
            if (chatMemoryRepository == null) {
                chatMemoryRepository = new InMemoryChatMemoryRepository();
            }
            return new TokenWindowChatMemory(chatMemoryRepository, maxTokens, maxMessages);
        }
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

```bash
cd backend && mvn test -Dtest=TokenWindowChatMemoryTest -q
```

Expected: All tests PASS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/tripweaver/chat/memory/TokenWindowChatMemory.java
git commit -m "feat: add TokenWindowChatMemory with token-based windowing"
```

---

### Task 3: Update production AiConfig

**Files:**
- Modify: `backend/src/main/java/com/tripweaver/config/AiConfig.java`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: Update AiConfig to use TokenWindowChatMemory**

```java
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
```

- [ ] **Step 2: Add config to application.yml**

Add after the existing `spring.ai.chat.memory.repository.jdbc.initialize-schema` block (line 28):

```yaml
        token-window:
          max-tokens: ${AI_CHAT_MEMORY_MAX_TOKENS:100000}
          max-messages: ${AI_CHAT_MEMORY_MAX_MESSAGES:20}
```

- [ ] **Step 3: Verify compilation**

```bash
cd backend && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/tripweaver/config/AiConfig.java backend/src/main/resources/application.yml
git commit -m "feat: replace MessageWindowChatMemory with TokenWindowChatMemory in production config"
```

---

### Task 4: Update test config and verify full test suite

**Files:**
- Modify: `backend/src/test/java/com/tripweaver/config/TestAiConfig.java`

- [ ] **Step 1: Update TestAiConfig to use TokenWindowChatMemory**

```java
package com.tripweaver.config;

import com.tripweaver.chat.memory.TokenWindowChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
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
```

- [ ] **Step 2: Run full test suite**

```bash
cd backend && mvn clean test -q
```

Expected: All tests PASS (BUILD SUCCESS).

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/tripweaver/config/TestAiConfig.java
git commit -m "test: update TestAiConfig to use TokenWindowChatMemory"
```
