package com.tripweaver.controller;

import com.tripweaver.entity.TravelPlan;
import com.tripweaver.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<TravelPlan>> getUserPlans() {
        return ResponseEntity.ok(planService.getUserPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlan> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlan> updatePlan(@PathVariable Long id, @RequestBody TravelPlan updates) {
        return ResponseEntity.ok(planService.updatePlan(id, updates));
    }
}