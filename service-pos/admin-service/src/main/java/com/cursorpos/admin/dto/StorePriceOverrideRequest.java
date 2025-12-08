package com.cursorpos.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for creating/updating store price overrides.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePriceOverrideRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Positive(message = "Override price must be positive")
    private BigDecimal overridePrice;

    @PositiveOrZero(message = "Discount percentage must be zero or positive")
    private BigDecimal discountPercentage;

    private Instant effectiveFrom;

    private Instant effectiveTo;

    @Builder.Default
    private Boolean isActive = true;
}
