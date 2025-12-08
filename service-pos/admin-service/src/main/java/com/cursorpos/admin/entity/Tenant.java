package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Tenant entity representing a business organization.
 * Each tenant is isolated and can have multiple customers, stores, and
 * branches.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenants_code", columnList = "code", unique = true),
        @Index(name = "idx_tenants_subdomain", columnList = "subdomain", unique = true),
        @Index(name = "idx_tenants_subscription_plan_id", columnList = "subscription_plan_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "subdomain", length = 50, unique = true)
    private String subdomain;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Subscription fields
    @Column(name = "subscription_plan_id")
    private UUID subscriptionPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", insertable = false, updatable = false)
    private transient SubscriptionPlan subscriptionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @Column(name = "subscription_start_date")
    private Instant subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private Instant subscriptionEndDate;

    // Localization
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "locale", length = 10)
    @Builder.Default
    private String locale = "en_US";

    // Loyalty configuration
    @Column(name = "loyalty_points_per_currency", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal loyaltyPointsPerCurrency = BigDecimal.ONE;

    @Column(name = "loyalty_enabled", nullable = false)
    @Builder.Default
    private Boolean loyaltyEnabled = true;

    /**
     * Subscription status enum.
     */
    public enum SubscriptionStatus {
        ACTIVE,
        TRIAL,
        EXPIRED,
        SUSPENDED,
        CANCELLED
    }

    /**
     * Check if the subscription is currently active.
     */
    public boolean hasActiveSubscription() {
        boolean isValidStatus = subscriptionStatus == SubscriptionStatus.ACTIVE
                || subscriptionStatus == SubscriptionStatus.TRIAL;
        boolean isNotExpired = subscriptionEndDate == null || !Instant.now().isAfter(subscriptionEndDate);
        return isValidStatus && isNotExpired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tenant tenant = (Tenant) o;
        return getId() != null && getId().equals(tenant.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
