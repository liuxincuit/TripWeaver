package com.tripweaver.repository;

import com.tripweaver.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<TravelPlan, Long> {

    List<TravelPlan> findByUserId(Long userId);

    Optional<TravelPlan> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("DELETE FROM TravelPlan p WHERE p.id = :id AND p.userId = :userId")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}