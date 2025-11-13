package com.cursorpos.identity.repository;

import com.cursorpos.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email and tenant ID.
     */
    Optional<User> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Checks if email exists for a tenant.
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Finds a user by ID and tenant ID.
     */
    Optional<User> findByIdAndTenantId(UUID id, String tenantId);

    /**
     * Counts active users for a tenant.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.isActive = true AND u.deletedAt IS NULL")
    long countActiveUsersByTenantId(String tenantId);
}
