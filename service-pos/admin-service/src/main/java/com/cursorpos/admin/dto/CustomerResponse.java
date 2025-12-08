package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for customer response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private UUID id;
    private String code;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private LocalDate dateOfBirth;
    private Boolean isActive;
    private String notes;

    // Loyalty fields
    private UUID loyaltyTierId;
    private LoyaltyTierResponse loyaltyTier;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer lifetimePoints;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}
