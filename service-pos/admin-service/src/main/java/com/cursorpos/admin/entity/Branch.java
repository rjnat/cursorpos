package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Branch entity representing a sub-location within a store.
 * Branches allow for departmental or location-based organization within a store.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "branches", indexes = {
        @Index(name = "idx_branches_tenant_code", columnList = "tenant_id,code", unique = true),
        @Index(name = "idx_branches_store_id", columnList = "store_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class Branch extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "branch_type", length = 50)
    private String branchType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;
}
