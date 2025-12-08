package com.cursorpos.admin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating loyalty tier.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Minimum points is required")
    @Min(value = 0, message = "Minimum points must be at least 0")
    private Integer minPoints;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.0", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    private BigDecimal discountPercentage;

    @NotNull(message = "Points multiplier is required")
    @DecimalMin(value = "0.1", message = "Points multiplier must be at least 0.1")
    private BigDecimal pointsMultiplier;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    private String benefits;

    private Integer displayOrder;
}
