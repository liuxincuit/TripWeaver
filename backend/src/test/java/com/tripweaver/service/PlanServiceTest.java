package com.tripweaver.service;

import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.exception.BusinessException;
import com.tripweaver.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PlanService planService;

    private User testUser;
    private TravelPlan testPlan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testPlan = new TravelPlan();
        testPlan.setId(1L);
        testPlan.setUserId(1L);
        testPlan.setTitle("测试计划");
        testPlan.setDestination("北京");
        testPlan.setStartDate(LocalDate.of(2026, 6, 1));
        testPlan.setEndDate(LocalDate.of(2026, 6, 5));
        testPlan.setContent("{\"days\":[]}");
    }

    @Test
    void getUserPlans_shouldReturnPlans() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByUserId(1L)).thenReturn(List.of(testPlan));

        List<TravelPlan> plans = planService.getUserPlans();

        assertEquals(1, plans.size());
        assertEquals("测试计划", plans.get(0).getTitle());
    }

    @Test
    void getPlanById_shouldReturnPlan_whenExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPlan));

        TravelPlan plan = planService.getPlanById(1L);

        assertEquals("测试计划", plan.getTitle());
    }

    @Test
    void getPlanById_shouldThrowException_whenNotExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> planService.getPlanById(999L));

        assertEquals("PLAN_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getPlanById_shouldThrowException_whenNotOwner() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> planService.getPlanById(1L));

        assertEquals("PLAN_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void deletePlan_shouldDelete() {
        when(userService.getCurrentUser()).thenReturn(testUser);

        planService.deletePlan(1L);

        verify(planRepository).deleteByIdAndUserId(1L, 1L);
    }

    @Test
    void savePlan_shouldReturnSavedPlan() {
        TravelPlan newPlan = new TravelPlan();
        newPlan.setUserId(1L);
        newPlan.setTitle("新计划");

        when(planRepository.save(newPlan)).thenAnswer(inv -> {
            TravelPlan saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TravelPlan result = planService.savePlan(newPlan);

        assertEquals(2L, result.getId());
        assertEquals("新计划", result.getTitle());
    }

    @Test
    void updatePlan_shouldUpdateAllFields() {
        TravelPlan updates = new TravelPlan();
        updates.setTitle("更新标题");
        updates.setDestination("上海");
        updates.setStartDate(LocalDate.of(2026, 7, 1));
        updates.setEndDate(LocalDate.of(2026, 7, 10));
        updates.setContent("{\"days\":[1,2,3]}");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(TravelPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        TravelPlan result = planService.updatePlan(1L, updates);

        assertEquals("更新标题", result.getTitle());
        assertEquals("上海", result.getDestination());
        assertEquals(LocalDate.of(2026, 7, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 7, 10), result.getEndDate());
        assertEquals("{\"days\":[1,2,3]}", result.getContent());
    }

    @Test
    void updatePlan_shouldUpdatePartialFields() {
        TravelPlan updates = new TravelPlan();
        updates.setTitle("仅更新标题");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(TravelPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        TravelPlan result = planService.updatePlan(1L, updates);

        assertEquals("仅更新标题", result.getTitle());
        assertEquals("北京", result.getDestination());
        assertEquals(LocalDate.of(2026, 6, 1), result.getStartDate());
    }

    @Test
    void updatePlan_shouldThrowException_whenNotExists() {
        TravelPlan updates = new TravelPlan();
        updates.setTitle("更新标题");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(planRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
            () -> planService.updatePlan(999L, updates));

        assertEquals("PLAN_NOT_FOUND", exception.getErrorCode());
    }
}
