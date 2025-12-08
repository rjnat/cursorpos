package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for loyalty tier response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierResponse {

    private UUID id;
    private String code;
    private String name;
    private Integer minPoints;
    private BigDecimal discountPercentage;
    private BigDecimal pointsMultiplier;
    private String color;
    private String icon;
    private String benefits;
    private Integer displayOrder;
    private Boolean isActive;
}
