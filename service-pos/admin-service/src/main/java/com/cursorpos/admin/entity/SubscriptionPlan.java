package com.cursorpos.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Subscription plan entity representing available plans for tenants.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_subscription_plans_code", columnList = "code", unique = true),
        @Index(name = "idx_subscription_plans_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_stores")
    private Integer maxStores;

    @Column(name = "max_products")
    private Integer maxProducts;

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceMonthly = BigDecimal.ZERO;

    @Column(name = "price_yearly", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceYearly = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "JSONB")
    private String features;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "SYSTEM";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    @Builder.Default
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Soft delete this entity.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if this plan has unlimited users.
     */
    public boolean hasUnlimitedUsers() {
        return maxUsers == null;
    }

    /**
     * Check if this plan has unlimited stores.
     */
    public boolean hasUnlimitedStores() {
        return maxStores == null;
    }

    /**
     * Check if this plan has unlimited products.
     */
    public boolean hasUnlimitedProducts() {
        return maxProducts == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SubscriptionPlan that))
            return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
