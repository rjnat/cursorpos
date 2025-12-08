package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.service.StorePriceOverrideService;
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
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for store price override management.
 * Handles store-specific product pricing.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/price-overrides")
@RequiredArgsConstructor
public class StorePriceOverrideController {

    private final StorePriceOverrideService priceOverrideService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_CREATE')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> createOverride(
            @Valid @RequestBody StorePriceOverrideRequest request) {
        StorePriceOverrideResponse response = priceOverrideService.createOverride(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Price override created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_READ')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> getOverrideById(@PathVariable UUID id) {
        StorePriceOverrideResponse response = priceOverrideService.getOverrideById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<StorePriceOverrideResponse>>> getOverridesByStore(
            @PathVariable UUID storeId,
            Pageable pageable) {
        PagedResponse<StorePriceOverrideResponse> response = priceOverrideService.getOverridesByStore(storeId,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<StorePriceOverrideResponse>>> getOverridesByProduct(
            @PathVariable UUID productId,
            Pageable pageable) {
        PagedResponse<StorePriceOverrideResponse> response = priceOverrideService.getOverridesByProduct(productId,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_READ')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> getActiveOverride(
            @RequestParam UUID storeId,
            @RequestParam UUID productId) {
        Optional<StorePriceOverrideResponse> response = priceOverrideService.getActiveOverride(storeId, productId);
        return response.map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No active override found")));
    }

    @GetMapping("/store/{storeId}/active")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_READ')")
    public ResponseEntity<ApiResponse<List<StorePriceOverrideResponse>>> getAllActiveOverridesForStore(
            @PathVariable UUID storeId) {
        List<StorePriceOverrideResponse> response = priceOverrideService.getAllActiveOverridesForStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_UPDATE')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> updateOverride(
            @PathVariable UUID id,
            @Valid @RequestBody StorePriceOverrideRequest request) {
        StorePriceOverrideResponse response = priceOverrideService.updateOverride(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Price override updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteOverride(@PathVariable UUID id) {
        priceOverrideService.deleteOverride(id);
        return ResponseEntity.ok(ApiResponse.success("Price override deleted successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_UPDATE')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> activateOverride(@PathVariable UUID id) {
        StorePriceOverrideResponse response = priceOverrideService.activateOverride(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Price override activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRICE_OVERRIDE_UPDATE')")
    public ResponseEntity<ApiResponse<StorePriceOverrideResponse>> deactivateOverride(@PathVariable UUID id) {
        StorePriceOverrideResponse response = priceOverrideService.deactivateOverride(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Price override deactivated successfully"));
    }
}
