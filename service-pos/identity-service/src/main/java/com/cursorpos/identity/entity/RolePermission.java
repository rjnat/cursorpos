package com.cursorpos.identity.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Role-Permission mapping entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "role_permissions", indexes = {
        @Index(name = "idx_role_permissions_role", columnList = "role_id"),
        @Index(name = "idx_role_permissions_permission", columnList = "permission_id"),
        @Index(name = "idx_role_permissions_unique", columnList = "role_id,permission_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RolePermission extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;
}
