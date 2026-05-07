package com.tripweaver.repository;

import com.tripweaver.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByPlanId(Long planId);

    Optional<Conversation> findByPlanIdAndUserId(Long planId, Long userId);
}