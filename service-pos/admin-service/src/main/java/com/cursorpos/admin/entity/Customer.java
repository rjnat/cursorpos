package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Customer entity representing a customer of the business.
 * Customers are tenant-wide (shared across all stores under the tenant).
 * Includes loyalty program fields.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_customers_tenant_email", columnList = "tenant_id, email"),
        @Index(name = "idx_customers_tenant_phone", columnList = "tenant_id, phone"),
        @Index(name = "idx_customers_loyalty_tier", columnList = "tenant_id, loyalty_tier_id"),
        @Index(name = "idx_customers_total_points", columnList = "tenant_id, total_points")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

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

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Loyalty fields
    @Column(name = "loyalty_tier_id")
    private UUID loyaltyTierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_tier_id", insertable = false, updatable = false)
    private LoyaltyTier loyaltyTier;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "available_points", nullable = false)
    @Builder.Default
    private Integer availablePoints = 0;

    @Column(name = "lifetime_points", nullable = false)
    @Builder.Default
    private Integer lifetimePoints = 0;

    /**
     * Gets the full name of the customer.
     */
    public String getFullName() {
        if (lastName != null && !lastName.isBlank()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Customer customer = (Customer) o;
        return getId() != null && getId().equals(customer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
