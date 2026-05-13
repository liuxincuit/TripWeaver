package com.tripweaver.repository;

import com.tripweaver.entity.TravelPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PlanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlanRepository planRepository;

    private TravelPlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new TravelPlan();
        testPlan.setUserId(1L);
        testPlan.setTitle("北京三日游");
        testPlan.setDestination("北京");
        testPlan.setStartDate(LocalDate.of(2026, 5, 1));
        testPlan.setEndDate(LocalDate.of(2026, 5, 3));
        testPlan.setContent("行程内容...");
    }

    @Test
    void shouldSavePlan() {
        TravelPlan saved = planRepository.save(testPlan);

        assertNotNull(saved.getId());
        assertEquals("北京三日游", saved.getTitle());
    }

    @Test
    void shouldFindByUserId() {
        planRepository.save(testPlan);

        List<TravelPlan> plans = planRepository.findByUserId(1L);

        assertEquals(1, plans.size());
        assertEquals("北京三日游", plans.get(0).getTitle());
    }

    @Test
    void shouldFindByIdAndUserId() {
        TravelPlan saved = planRepository.save(testPlan);

        Optional<TravelPlan> found = planRepository.findByIdAndUserId(saved.getId(), 1L);

        assertTrue(found.isPresent());
        assertEquals("北京三日游", found.get().getTitle());
    }

    @Test
    void shouldNotFindPlanForOtherUser() {
        TravelPlan saved = planRepository.save(testPlan);

        Optional<TravelPlan> found = planRepository.findByIdAndUserId(saved.getId(), 2L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldDeleteByIdAndUserId() {
        TravelPlan saved = entityManager.persistAndFlush(testPlan);
        Long savedId = saved.getId();

        int deleted = planRepository.deleteByIdAndUserId(savedId, 1L);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, deleted);
        Optional<TravelPlan> found = planRepository.findById(savedId);
        assertFalse(found.isPresent());
    }
}