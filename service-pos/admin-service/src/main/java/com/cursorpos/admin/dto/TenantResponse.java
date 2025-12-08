package com.cursorpos.admin.dto;

import com.cursorpos.admin.entity.Tenant.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for tenant response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private UUID id;
    private String code;
    private String name;
    private String subdomain;
    private String businessType;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String taxId;
    private Boolean isActive;

    // Subscription fields
    private UUID subscriptionPlanId;
    private SubscriptionPlanResponse subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private Instant subscriptionStartDate;
    private Instant subscriptionEndDate;
    private Boolean hasActiveSubscription;

    // Localization
    private String logoUrl;
    private String timezone;
    private String currency;
    private String locale;

    // Loyalty configuration
    private BigDecimal loyaltyPointsPerCurrency;
    private Boolean loyaltyEnabled;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}
