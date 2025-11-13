package com.cursorpos.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for price history.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryResponse {

    private UUID id;
    private String tenantId;
    private UUID productId;
    private String productCode;
    private String productName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String changedBy;
    private String reason;
    private Instant createdAt;
}
