package com.cursorpos.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for inventory.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private UUID id;
    private String tenantId;
    private UUID productId;
    private String productCode;
    private String productName;
    private UUID branchId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer quantityAvailable;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private Boolean isLowStock;
    private Instant createdAt;
    private Instant updatedAt;
}
