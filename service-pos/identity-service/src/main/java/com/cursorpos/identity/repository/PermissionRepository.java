package com.cursorpos.identity.repository;

import com.cursorpos.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Finds a permission by code and tenant ID.
     */
    Optional<Permission> findByCodeAndTenantId(String code, String tenantId);

    /**
     * Checks if permission code exists for a tenant.
     */
    boolean existsByCodeAndTenantId(String code, String tenantId);

    /**
     * Finds all permissions for a list of role IDs.
     */
    List<Permission> findByIdIn(List<UUID> permissionIds);
}
