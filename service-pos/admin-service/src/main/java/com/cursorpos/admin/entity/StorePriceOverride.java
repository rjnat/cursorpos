package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Store price override entity for store-specific product pricing.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "store_price_overrides", indexes = {
        @Index(name = "idx_store_price_overrides_tenant_store", columnList = "tenant_id, store_id"),
        @Index(name = "idx_store_price_overrides_product", columnList = "tenant_id, product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePriceOverride extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "override_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal overridePrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "effective_from", nullable = false)
    @Builder.Default
    private Instant effectiveFrom = Instant.now();

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if this override is currently effective.
     */
    public boolean isEffective() {
        Instant now = Instant.now();
        boolean afterStart = !now.isBefore(effectiveFrom);
        boolean beforeEnd = effectiveTo == null || now.isBefore(effectiveTo);
        return isActive && afterStart && beforeEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StorePriceOverride that = (StorePriceOverride) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
