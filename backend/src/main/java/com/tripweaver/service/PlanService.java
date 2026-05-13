package com.tripweaver.service;

import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.exception.BusinessException;
import com.tripweaver.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserService userService;
    private final ChatMemory chatMemory;

    public List<TravelPlan> getUserPlans() {
        User user = userService.getCurrentUser();
        return planRepository.findByUserId(user.getId());
    }

    public TravelPlan getPlanById(Long id) {
        User user = userService.getCurrentUser();
        return planRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException("计划不存在", "PLAN_NOT_FOUND"));
    }

    @Transactional
    public void deletePlan(Long id) {
        User user = userService.getCurrentUser();
        planRepository.deleteByIdAndUserId(id, user.getId());
        try {
            chatMemory.clear(String.valueOf(id));
        } catch (Exception e) {
            log.warn("清理计划 {} 的聊天记忆失败: {}", id, e.getMessage());
        }
    }

    public TravelPlan savePlan(TravelPlan plan) {
        return planRepository.save(plan);
    }

    public TravelPlan updatePlan(Long id, TravelPlan updates) {
        TravelPlan plan = getPlanById(id);
        if (updates.getTitle() != null) {
            plan.setTitle(updates.getTitle());
        }
        if (updates.getDestination() != null) {
            plan.setDestination(updates.getDestination());
        }
        if (updates.getStartDate() != null) {
            plan.setStartDate(updates.getStartDate());
        }
        if (updates.getEndDate() != null) {
            plan.setEndDate(updates.getEndDate());
        }
        if (updates.getContent() != null) {
            plan.setContent(updates.getContent());
        }
        return planRepository.save(plan);
    }
}