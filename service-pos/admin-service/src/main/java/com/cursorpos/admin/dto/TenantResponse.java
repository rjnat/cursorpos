package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String subscriptionPlan;
    private Instant subscriptionStartDate;
    private Instant subscriptionEndDate;
    private Integer maxUsers;
    private Integer maxStores;
    private Integer maxBranches;
    private String logoUrl;
    private String timezone;
    private String currency;
    private String locale;
    private Instant createdAt;
    private Instant updatedAt;
}
