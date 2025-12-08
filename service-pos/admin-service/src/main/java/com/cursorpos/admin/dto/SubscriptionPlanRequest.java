package com.cursorpos.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating/updating subscription plans.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRequest {

    @NotBlank(message = "Plan code is required")
    private String code;

    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    @NotNull(message = "Max users is required")
    private Integer maxUsers;

    @NotNull(message = "Max stores is required")
    private Integer maxStores;

    @NotNull(message = "Max products is required")
    private Integer maxProducts;

    @NotNull(message = "Monthly price is required")
    @PositiveOrZero(message = "Monthly price must be zero or positive")
    private BigDecimal priceMonthly;

    @NotNull(message = "Yearly price is required")
    @PositiveOrZero(message = "Yearly price must be zero or positive")
    private BigDecimal priceYearly;

    private List<String> features;

    @Builder.Default
    private Boolean isActive = true;

    @PositiveOrZero(message = "Trial days must be zero or positive")
    @Builder.Default
    private Integer trialDays = 0;

    @Positive(message = "Display order must be positive")
    @Builder.Default
    private Integer displayOrder = 1;
}
