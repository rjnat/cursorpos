package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
import com.cursorpos.admin.service.SubscriptionPlanService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for subscription plan management.
 * Subscription plans are global (not tenant-specific).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_CREATE')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse response = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subscription plan created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_READ')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(@PathVariable UUID id) {
        SubscriptionPlanResponse response = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{planCode}")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_READ')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanByCode(@PathVariable String planCode) {
        SubscriptionPlanResponse response = subscriptionPlanService.getPlanByCode(planCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<SubscriptionPlanResponse>>> getAllPlans(Pageable pageable) {
        PagedResponse<SubscriptionPlanResponse> response = subscriptionPlanService.getAllPlans(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans() {
        List<SubscriptionPlanResponse> response = subscriptionPlanService.getActivePlans();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_UPDATE')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse response = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription plan updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID id) {
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_UPDATE')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> activatePlan(@PathVariable UUID id) {
        SubscriptionPlanResponse response = subscriptionPlanService.activatePlan(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription plan activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_UPDATE')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> deactivatePlan(@PathVariable UUID id) {
        SubscriptionPlanResponse response = subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription plan deactivated successfully"));
    }

    @GetMapping("/{targetPlanId}/can-change")
    @PreAuthorize("hasAuthority('SUBSCRIPTION_PLAN_READ')")
    public ResponseEntity<ApiResponse<Boolean>> canChangePlan(
            @PathVariable UUID targetPlanId,
            @RequestParam int currentUsers,
            @RequestParam int currentStores,
            @RequestParam int currentProducts) {
        boolean canChange = subscriptionPlanService.canChangePlan(
                targetPlanId, currentUsers, currentStores, currentProducts);
        return ResponseEntity.ok(ApiResponse.success(canChange));
    }
}
