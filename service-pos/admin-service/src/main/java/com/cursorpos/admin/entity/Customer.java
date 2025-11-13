package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Customer entity representing a customer of the business.
 * Customers can be individuals or organizations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_tenant_email", columnList = "tenant_id,email"),
        @Index(name = "idx_customers_tenant_phone", columnList = "tenant_id,phone"),
        @Index(name = "idx_customers_tenant_code", columnList = "tenant_id,code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Customer extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "customer_type", nullable = false, length = 20)
    @Builder.Default
    private String customerType = "INDIVIDUAL";

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 200)
    private String companyName;

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

    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Gets the full name of the customer.
     */
    public String getFullName() {
        if (customerType.equals("INDIVIDUAL")) {
            return firstName + " " + lastName;
        }
        return companyName;
    }
}
