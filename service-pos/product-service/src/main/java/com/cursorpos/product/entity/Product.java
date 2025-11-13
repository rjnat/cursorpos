package com.cursorpos.product.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Product entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_tenant", columnList = "tenant_id"),
    @Index(name = "idx_product_code", columnList = "tenant_id,code"),
    @Index(name = "idx_product_sku", columnList = "tenant_id,sku"),
    @Index(name = "idx_product_category", columnList = "tenant_id,category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(precision = 19, scale = 4)
    private BigDecimal cost;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(length = 50)
    private String unit;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_trackable", nullable = false)
    @Builder.Default
    private Boolean isTrackable = true;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
}
