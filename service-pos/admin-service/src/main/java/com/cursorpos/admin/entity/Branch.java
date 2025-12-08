package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Branch entity representing a regional grouping within a tenant.
 * Branches contain multiple stores and allow for regional reporting.
 * Hierarchy: Tenant → Branch → Store
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "branches", indexes = {
        @Index(name = "idx_branches_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_branches_is_active", columnList = "tenant_id, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

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

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Branch branch = (Branch) o;
        return getId() != null && getId().equals(branch.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
