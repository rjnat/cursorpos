package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Loyalty tier entity representing loyalty program tiers for customers.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "loyalty_tiers", indexes = {
        @Index(name = "idx_loyalty_tiers_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_loyalty_tiers_min_points", columnList = "tenant_id, min_points")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTier extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "min_points", nullable = false)
    @Builder.Default
    private Integer minPoints = 0;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "points_multiplier", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pointsMultiplier = BigDecimal.ONE;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "benefits", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String benefits;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LoyaltyTier that = (LoyaltyTier) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
