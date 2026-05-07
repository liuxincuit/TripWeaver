package com.tripweaver.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    @Test
    void shouldCreateConversationWithAllFields() {
        Conversation conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUserId(1L);
        conversation.setPlanId(1L);
        conversation.setMessages("[{\"role\":\"user\",\"content\":\"hello\"}]");

        assertEquals(1L, conversation.getId());
        assertEquals(1L, conversation.getUserId());
        assertEquals(1L, conversation.getPlanId());
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", conversation.getMessages());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        Conversation conversation = new Conversation();
        conversation.onCreate();

        assertNotNull(conversation.getCreatedAt());
    }
}