package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.*;
import com.cursorpos.admin.service.LoyaltyService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for loyalty program management.
 * Handles loyalty tiers and transactions.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // ========== Loyalty Tier Endpoints ==========

    @PostMapping("/tiers")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_CREATE')")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> createTier(
            @Valid @RequestBody LoyaltyTierRequest request) {
        LoyaltyTierResponse response = loyaltyService.createTier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Loyalty tier created successfully"));
    }

    @GetMapping("/tiers/{id}")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_READ')")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> getTierById(@PathVariable UUID id) {
        LoyaltyTierResponse response = loyaltyService.getTierById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tiers/code/{code}")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_READ')")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> getTierByCode(@PathVariable String code) {
        LoyaltyTierResponse response = loyaltyService.getTierByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tiers")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTierResponse>>> getAllTiers(Pageable pageable) {
        PagedResponse<LoyaltyTierResponse> response = loyaltyService.getAllTiers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tiers/ordered")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_READ')")
    public ResponseEntity<ApiResponse<List<LoyaltyTierResponse>>> getAllTiersOrdered() {
        List<LoyaltyTierResponse> response = loyaltyService.getAllTiersOrdered();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/tiers/{id}")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_UPDATE')")
    public ResponseEntity<ApiResponse<LoyaltyTierResponse>> updateTier(
            @PathVariable UUID id,
            @Valid @RequestBody LoyaltyTierRequest request) {
        LoyaltyTierResponse response = loyaltyService.updateTier(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Loyalty tier updated successfully"));
    }

    @DeleteMapping("/tiers/{id}")
    @PreAuthorize("hasAuthority('LOYALTY_TIER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable UUID id) {
        loyaltyService.deleteTier(id);
        return ResponseEntity.ok(ApiResponse.success("Loyalty tier deleted successfully"));
    }

    // ========== Loyalty Transaction Endpoints ==========

    @PostMapping("/transactions")
    @PreAuthorize("hasAuthority('LOYALTY_TRANSACTION_CREATE')")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> createTransaction(
            @Valid @RequestBody LoyaltyTransactionRequest request) {
        LoyaltyTransactionResponse response = loyaltyService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Loyalty transaction created successfully"));
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('LOYALTY_TRANSACTION_READ')")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> getTransactionById(@PathVariable UUID id) {
        LoyaltyTransactionResponse response = loyaltyService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/customer/{customerId}")
    @PreAuthorize("hasAuthority('LOYALTY_TRANSACTION_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>> getTransactionsByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        PagedResponse<LoyaltyTransactionResponse> response = loyaltyService.getTransactionsByCustomer(customerId,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('LOYALTY_TRANSACTION_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>> getAllTransactions(
            Pageable pageable) {
        PagedResponse<LoyaltyTransactionResponse> response = loyaltyService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Utility Endpoints ==========

    @GetMapping("/calculate-points")
    @PreAuthorize("hasAuthority('LOYALTY_TRANSACTION_READ')")
    public ResponseEntity<ApiResponse<Integer>> calculatePointsForPurchase(
            @RequestParam UUID customerId,
            @RequestParam BigDecimal purchaseAmount,
            @RequestParam BigDecimal loyaltyPointsPerCurrency) {
        int points = loyaltyService.calculatePointsForPurchase(customerId, purchaseAmount, loyaltyPointsPerCurrency);
        return ResponseEntity.ok(ApiResponse.success(points));
    }
}
