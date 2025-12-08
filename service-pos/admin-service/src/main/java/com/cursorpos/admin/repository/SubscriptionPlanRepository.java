package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SubscriptionPlan entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByCodeAndDeletedAtIsNull(String code);

    Optional<SubscriptionPlan> findByIdAndDeletedAtIsNull(UUID id);

    Page<SubscriptionPlan> findByDeletedAtIsNull(Pageable pageable);

    List<SubscriptionPlan> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    Page<SubscriptionPlan> findByIsActiveAndDeletedAtIsNull(Boolean isActive, Pageable pageable);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    List<SubscriptionPlan> findByDeletedAtIsNullOrderByDisplayOrderAsc();
}
