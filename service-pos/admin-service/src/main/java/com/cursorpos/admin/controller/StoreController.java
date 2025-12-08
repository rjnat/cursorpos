package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.service.StoreService;
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
 * REST controller for store management.
 * Stores belong to branches and can have specific pricing configurations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasAuthority('STORE_CREATE')")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Store created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable UUID id) {
        StoreResponse response = storeService.getStoreById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreByCode(@PathVariable String code) {
        StoreResponse response = storeService.getStoreByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<StoreResponse>>> getAllStores(Pageable pageable) {
        PagedResponse<StoreResponse> response = storeService.getAllStores(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<StoreResponse>>> getStoresByBranch(
            @PathVariable UUID branchId,
            Pageable pageable) {
        PagedResponse<StoreResponse> response = storeService.getStoresByBranch(branchId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/branch/{branchId}/active")
    @PreAuthorize("hasAuthority('STORE_READ')")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getActiveStoresByBranch(@PathVariable UUID branchId) {
        List<StoreResponse> response = storeService.getActiveStoresByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable UUID id,
            @Valid @RequestBody CreateStoreRequest request) {
        StoreResponse response = storeService.updateStore(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Store updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.success("Store deleted successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<ApiResponse<StoreResponse>> activateStore(@PathVariable UUID id) {
        StoreResponse response = storeService.activateStore(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Store activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<ApiResponse<StoreResponse>> deactivateStore(@PathVariable UUID id) {
        StoreResponse response = storeService.deactivateStore(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Store deactivated successfully"));
    }
}
