package com.tripweaver.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TravelPlanTest {

    @Test
    void shouldCreateTravelPlanWithAllFields() {
        TravelPlan plan = new TravelPlan();
        plan.setId(1L);
        plan.setUserId(1L);
        plan.setTitle("北京三日游");
        plan.setDestination("北京");
        plan.setStartDate(LocalDate.of(2026, 5, 1));
        plan.setEndDate(LocalDate.of(2026, 5, 3));
        plan.setContent("行程内容...");

        assertEquals(1L, plan.getId());
        assertEquals(1L, plan.getUserId());
        assertEquals("北京三日游", plan.getTitle());
        assertEquals("北京", plan.getDestination());
        assertEquals(LocalDate.of(2026, 5, 1), plan.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 3), plan.getEndDate());
        assertEquals("行程内容...", plan.getContent());
    }

    @Test
    void shouldSetTimestampsOnPrePersist() {
        TravelPlan plan = new TravelPlan();
        plan.onCreate();

        assertNotNull(plan.getCreatedAt());
        assertNotNull(plan.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() {
        TravelPlan plan = new TravelPlan();
        plan.onCreate();
        LocalDateTime created = plan.getCreatedAt();

        plan.onUpdate();

        assertEquals(created, plan.getCreatedAt());
        assertTrue(plan.getUpdatedAt().isAfter(created) || plan.getUpdatedAt().equals(created));
    }
}