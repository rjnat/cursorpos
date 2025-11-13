package com.cursorpos.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for products.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;
    private String tenantId;
    private String code;
    private String sku;
    private String name;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal cost;
    private BigDecimal taxRate;
    private String unit;
    private String barcode;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isTrackable;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Instant createdAt;
    private Instant updatedAt;
}
