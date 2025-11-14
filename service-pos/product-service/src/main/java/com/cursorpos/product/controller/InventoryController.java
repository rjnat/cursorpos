package com.cursorpos.product.controller;

import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.InventoryResponse;
import com.cursorpos.product.dto.StockAdjustmentRequest;
import com.cursorpos.product.service.InventoryService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for inventory management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> createOrUpdateInventory(
            @Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.createOrUpdateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Inventory created/updated successfully"));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {
        InventoryResponse response = inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully"));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @RequestParam UUID productId,
            @RequestParam UUID branchId,
            @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.reserveStock(productId, branchId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock reserved successfully"));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseStock(
            @RequestParam UUID productId,
            @RequestParam UUID branchId,
            @RequestParam Integer quantity) {
        InventoryResponse response = inventoryService.releaseStock(productId, branchId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock released successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable UUID id) {
        InventoryResponse response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/product/{productId}/branch/{branchId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductAndBranch(
            @PathVariable UUID productId,
            @PathVariable UUID branchId) {
        InventoryResponse response = inventoryService.getInventoryByProductAndBranch(productId, branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InventoryResponse>>> getAllInventory(Pageable pageable) {
        PagedResponse<InventoryResponse> response = inventoryService.getAllInventory(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryResponse>>> getInventoryByBranch(
            @PathVariable UUID branchId,
            Pageable pageable) {
        PagedResponse<InventoryResponse> response = inventoryService.getInventoryByBranch(branchId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProduct(@PathVariable UUID productId) {
        List<InventoryResponse> response = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockItems() {
        List<InventoryResponse> response = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/low-stock/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockItemsByBranch(@PathVariable UUID branchId) {
        List<InventoryResponse> response = inventoryService.getLowStockItemsByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
