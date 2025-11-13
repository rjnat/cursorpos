package com.cursorpos.identity.repository;

import com.cursorpos.identity.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UserRole entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /**
     * Finds all user roles for a user.
     */
    List<UserRole> findByUserIdAndTenantId(UUID userId, String tenantId);

    /**
     * Finds all roles for a user with role details.
     */
    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId AND ur.tenantId = :tenantId AND ur.deletedAt IS NULL")
    List<UUID> findRoleIdsByUserIdAndTenantId(UUID userId, String tenantId);

    /**
     * Checks if user has a specific role.
     */
    boolean existsByUserIdAndRoleIdAndTenantId(UUID userId, UUID roleId, String tenantId);

    /**
     * Deletes user role mapping.
     */
    void deleteByUserIdAndRoleIdAndTenantId(UUID userId, UUID roleId, String tenantId);
}
