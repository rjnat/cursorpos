package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Store entity representing a physical store location.
 * Stores belong to branches and can have specific pricing configurations.
 * Hierarchy: Tenant → Branch → Store
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_stores_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_stores_branch_id", columnList = "branch_id"),
        @Index(name = "idx_stores_is_active", columnList = "tenant_id, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", insertable = false, updatable = false)
    private Branch branch;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "store_type", length = 50)
    private String storeType;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    @Column(name = "operating_hours", length = 500)
    private String operatingHours;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "global_discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal globalDiscountPercentage = BigDecimal.ZERO;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Store store = (Store) o;
        return getId() != null && getId().equals(store.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
