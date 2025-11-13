package com.cursorpos.identity.repository;

import com.cursorpos.identity.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RolePermission entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    /**
     * Finds all permission IDs for a role.
     */
    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId AND rp.tenantId = :tenantId AND rp.deletedAt IS NULL")
    List<UUID> findPermissionIdsByRoleIdAndTenantId(UUID roleId, String tenantId);

    /**
     * Finds all permissions for multiple roles.
     */
    @Query("SELECT DISTINCT rp.permissionId FROM RolePermission rp WHERE rp.roleId IN :roleIds AND rp.tenantId = :tenantId AND rp.deletedAt IS NULL")
    List<UUID> findPermissionIdsByRoleIdsAndTenantId(List<UUID> roleIds, String tenantId);

    /**
     * Checks if role has a specific permission.
     */
    boolean existsByRoleIdAndPermissionIdAndTenantId(UUID roleId, UUID permissionId, String tenantId);
}
