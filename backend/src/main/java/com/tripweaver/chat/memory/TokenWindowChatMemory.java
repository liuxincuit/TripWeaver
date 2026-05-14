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

        while (result.size() > maxMessages) {
            removeOldestNonSystemMessage(result);
        }

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
