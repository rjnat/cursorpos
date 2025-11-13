package com.cursorpos.admin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for creating a new tenant.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Code must contain only lowercase letters, numbers, and hyphens")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 50, message = "Subdomain must not exceed 50 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
    private String subdomain;

    @Size(max = 50, message = "Business type must not exceed 50 characters")
    private String businessType;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Size(max = 50, message = "Subscription plan must not exceed 50 characters")
    private String subscriptionPlan;

    private Instant subscriptionStartDate;

    private Instant subscriptionEndDate;

    @Min(value = 1, message = "Max users must be at least 1")
    private Integer maxUsers;

    @Min(value = 1, message = "Max stores must be at least 1")
    private Integer maxStores;

    @Min(value = 1, message = "Max branches must be at least 1")
    private Integer maxBranches;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    private String currency;

    @Size(max = 10, message = "Locale must not exceed 10 characters")
    private String locale;
}
