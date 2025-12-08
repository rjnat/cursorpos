package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for store price override response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePriceOverrideResponse {

    private UUID id;
    private UUID storeId;
    private String storeName;
    private UUID productId;
    private String productName;
    private BigDecimal overridePrice;
    private BigDecimal discountPercentage;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
