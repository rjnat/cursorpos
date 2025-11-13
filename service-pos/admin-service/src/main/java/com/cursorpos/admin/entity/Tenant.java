package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tenant entity representing a business organization.
 * Each tenant is isolated and can have multiple customers, stores, and
 * branches.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenants_code", columnList = "code", unique = true),
        @Index(name = "idx_tenants_subdomain", columnList = "subdomain", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    @Column(name = "email", length = 255)
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

    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan;

    @Column(name = "subscription_start_date")
    private Instant subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private Instant subscriptionEndDate;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_stores")
    private Integer maxStores;

    @Column(name = "max_branches")
    private Integer maxBranches;

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
}
