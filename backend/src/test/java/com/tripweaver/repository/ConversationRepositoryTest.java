package com.tripweaver.repository;

import com.tripweaver.entity.Conversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setUserId(1L);
        testConversation.setPlanId(1L);
        testConversation.setMessages("[{\"role\":\"user\",\"content\":\"hello\"}]");
    }

    @Test
    void shouldSaveConversation() {
        Conversation saved = conversationRepository.save(testConversation);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getUserId());
    }

    @Test
    void shouldFindByPlanId() {
        Conversation saved = conversationRepository.save(testConversation);

        Optional<Conversation> found = conversationRepository.findByPlanId(saved.getPlanId());

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void shouldFindByPlanIdAndUserId() {
        Conversation saved = conversationRepository.save(testConversation);

        Optional<Conversation> found = conversationRepository.findByPlanIdAndUserId(saved.getPlanId(), 1L);

        assertTrue(found.isPresent());
    }

    @Test
    void shouldNotFindConversationForOtherUser() {
        Conversation saved = conversationRepository.save(testConversation);

        Optional<Conversation> found = conversationRepository.findByPlanIdAndUserId(saved.getPlanId(), 2L);

        assertFalse(found.isPresent());
    }
}