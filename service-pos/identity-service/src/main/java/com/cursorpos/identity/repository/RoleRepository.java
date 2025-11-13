package com.cursorpos.identity.repository;

import com.cursorpos.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds a role by code and tenant ID.
     */
    Optional<Role> findByCodeAndTenantId(String code, String tenantId);

    /**
     * Finds a role by ID and tenant ID.
     */
    Optional<Role> findByIdAndTenantId(UUID id, String tenantId);

    /**
     * Checks if role code exists for a tenant.
     */
    boolean existsByCodeAndTenantId(String code, String tenantId);
}
