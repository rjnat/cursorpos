package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
import com.cursorpos.admin.entity.SubscriptionPlan;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.SubscriptionPlanRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing subscription plans.
 * Subscription plans are global (not tenant-specific).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private static final String PLAN_NOT_FOUND_MSG = "Subscription plan not found with ID: ";

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        Objects.requireNonNull(request, "request");
        log.info("Creating subscription plan with code: {}", request.getCode());

        if (subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new IllegalArgumentException("Plan with code " + request.getCode() + " already exists");
        }

        SubscriptionPlan plan = adminMapper.toSubscriptionPlan(request);
        @SuppressWarnings("null")
        SubscriptionPlan saved = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan created successfully with ID: {}", saved.getId());
        return adminMapper.toSubscriptionPlanResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID id) {
        Objects.requireNonNull(id, "id");
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + id));
        return adminMapper.toSubscriptionPlanResponse(plan);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanByCode(String code) {
        Objects.requireNonNull(code, "code");
        SubscriptionPlan plan = subscriptionPlanRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with code: " + code));
        return adminMapper.toSubscriptionPlanResponse(plan);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SubscriptionPlanResponse> getAllPlans(Pageable pageable) {
        Page<SubscriptionPlan> page = subscriptionPlanRepository.findByDeletedAtIsNull(pageable);
        return PagedResponse.of(page.map(adminMapper::toSubscriptionPlanResponse));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getActivePlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByDeletedAtIsNullOrderByDisplayOrderAsc();
        return plans.stream()
                .filter(SubscriptionPlan::getIsActive)
                .map(adminMapper::toSubscriptionPlanResponse)
                .toList();
    }

    @Transactional
    public SubscriptionPlanResponse updatePlan(UUID id, SubscriptionPlanRequest request) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        log.info("Updating subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + id));

        // Check if code is being changed to an existing code
        if (!plan.getCode().equals(request.getCode()) &&
                subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new IllegalArgumentException("Plan with code " + request.getCode() + " already exists");
        }

        adminMapper.updateSubscriptionPlanFromRequest(request, plan);
        SubscriptionPlan updated = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan updated successfully with ID: {}", updated.getId());
        return adminMapper.toSubscriptionPlanResponse(updated);
    }

    @Transactional
    public void deletePlan(UUID id) {
        Objects.requireNonNull(id, "id");
        log.info("Deleting subscription plan with ID: {}", id);

        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + id));

        plan.softDelete();
        subscriptionPlanRepository.save(plan);

        log.info("Subscription plan soft-deleted successfully with ID: {}", id);
    }

    @Transactional
    public SubscriptionPlanResponse activatePlan(UUID id) {
        Objects.requireNonNull(id, "id");
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + id));
        plan.setIsActive(true);
        SubscriptionPlan updated = subscriptionPlanRepository.save(plan);
        return adminMapper.toSubscriptionPlanResponse(updated);
    }

    @Transactional
    public SubscriptionPlanResponse deactivatePlan(UUID id) {
        Objects.requireNonNull(id, "id");
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + id));
        plan.setIsActive(false);
        SubscriptionPlan updated = subscriptionPlanRepository.save(plan);
        return adminMapper.toSubscriptionPlanResponse(updated);
    }

    /**
     * Check if a tenant can upgrade/downgrade to a target plan.
     * Block downgrade if current usage exceeds target plan limits.
     */
    @Transactional(readOnly = true)
    public boolean canChangePlan(UUID targetPlanId, int currentUsers, int currentStores, int currentProducts) {
        SubscriptionPlan targetPlan = subscriptionPlanRepository.findByIdAndDeletedAtIsNull(targetPlanId)
                .orElseThrow(() -> new ResourceNotFoundException(PLAN_NOT_FOUND_MSG + targetPlanId));

        // -1 means unlimited
        boolean usersOk = targetPlan.getMaxUsers() == -1 || currentUsers <= targetPlan.getMaxUsers();
        boolean storesOk = targetPlan.getMaxStores() == -1 || currentStores <= targetPlan.getMaxStores();
        boolean productsOk = targetPlan.getMaxProducts() == -1 || currentProducts <= targetPlan.getMaxProducts();

        return usersOk && storesOk && productsOk;
    }
}
