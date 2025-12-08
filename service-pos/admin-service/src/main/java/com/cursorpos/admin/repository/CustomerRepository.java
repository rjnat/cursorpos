package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Customer entity.
 * Customers are tenant-wide (shared across all stores).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Optional<Customer> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Page<Customer> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Customer> findByTenantIdAndIsActiveAndDeletedAtIsNull(String tenantId, Boolean isActive, Pageable pageable);

    boolean existsByTenantIdAndCode(String tenantId, String code);

    Optional<Customer> findByTenantIdAndEmailAndDeletedAtIsNull(String tenantId, String email);

    Optional<Customer> findByTenantIdAndPhoneAndDeletedAtIsNull(String tenantId, String phone);

    // Loyalty-related queries
    Page<Customer> findByTenantIdAndLoyaltyTierIdAndDeletedAtIsNull(String tenantId, UUID loyaltyTierId,
            Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.totalPoints >= :minPoints AND c.deletedAt IS NULL")
    List<Customer> findByTenantIdAndMinPointsAndDeletedAtIsNull(@Param("tenantId") String tenantId,
            @Param("minPoints") Integer minPoints);

    long countByTenantIdAndDeletedAtIsNull(String tenantId);
}
