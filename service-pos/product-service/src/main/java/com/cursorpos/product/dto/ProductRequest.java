package com.cursorpos.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating/updating products.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private UUID categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    private BigDecimal cost;

    @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Tax rate must not exceed 100")
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isTrackable = true;

    private Integer minStockLevel;

    private Integer maxStockLevel;
}
