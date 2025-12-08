package com.cursorpos.admin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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

    @NotBlank(message = "Email is required")
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

    // Subscription fields
    private UUID subscriptionPlanId;

    private Instant subscriptionStartDate;

    private Instant subscriptionEndDate;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    @Builder.Default
    private String timezone = "UTC";

    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Builder.Default
    private String currency = "USD";

    @Size(max = 10, message = "Locale must not exceed 10 characters")
    @Builder.Default
    private String locale = "en_US";

    // Loyalty configuration
    @DecimalMin(value = "0.01", message = "Loyalty points per currency must be at least 0.01")
    @DecimalMax(value = "100.00", message = "Loyalty points per currency must not exceed 100")
    @Builder.Default
    private BigDecimal loyaltyPointsPerCurrency = BigDecimal.ONE;

    @Builder.Default
    private Boolean loyaltyEnabled = true;
}
