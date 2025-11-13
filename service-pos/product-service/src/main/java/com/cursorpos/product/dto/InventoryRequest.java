package com.cursorpos.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for inventory adjustments.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Quantity on hand is required")
    @Min(value = 0, message = "Quantity on hand must be non-negative")
    private Integer quantityOnHand;

    @Min(value = 0, message = "Quantity reserved must be non-negative")
    @Builder.Default
    private Integer quantityReserved = 0;

    private Integer reorderPoint;

    private Integer reorderQuantity;
}
