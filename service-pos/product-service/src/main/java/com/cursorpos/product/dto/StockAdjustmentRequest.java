package com.cursorpos.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for stock adjustments.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType type;

    private String reason;

    public enum AdjustmentType {
        ADD,
        SUBTRACT,
        SET
    }
}
