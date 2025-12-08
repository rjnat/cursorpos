package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for subscription plan response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer maxUsers;
    private Integer maxStores;
    private Integer maxProducts;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private Boolean isActive;
    private Integer displayOrder;
    private String features;
    private Instant createdAt;
    private Instant updatedAt;
}
