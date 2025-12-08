package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for store response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private UUID id;
    private UUID branchId;
    private String branchName;
    private String code;
    private String name;
    private String description;
    private String storeType;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    private String operatingHours;
    private String timezone;
    private String currency;
    private BigDecimal taxRate;
    private BigDecimal globalDiscountPercentage;
    private Instant createdAt;
    private Instant updatedAt;
}
