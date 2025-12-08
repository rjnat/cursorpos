package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.LoyaltyTransaction;
import com.cursorpos.admin.entity.LoyaltyTransaction.LoyaltyTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LoyaltyTransaction entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    Optional<LoyaltyTransaction> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Page<LoyaltyTransaction> findByTenantIdAndCustomerIdAndDeletedAtIsNull(
            String tenantId, UUID customerId, Pageable pageable);

    Page<LoyaltyTransaction> findByTenantIdAndCustomerIdAndTransactionTypeAndDeletedAtIsNull(
            String tenantId, UUID customerId, LoyaltyTransactionType transactionType, Pageable pageable);

    Page<LoyaltyTransaction> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    /**
     * Sum points earned by a customer.
     */
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt " +
            "WHERE lt.tenantId = :tenantId AND lt.customerId = :customerId " +
            "AND lt.transactionType IN ('EARN', 'BONUS', 'ADJUSTMENT') AND lt.deletedAt IS NULL")
    Integer sumEarnedPoints(@Param("tenantId") String tenantId, @Param("customerId") UUID customerId);

    /**
     * Sum points redeemed by a customer.
     */
    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt " +
            "WHERE lt.tenantId = :tenantId AND lt.customerId = :customerId " +
            "AND lt.transactionType IN ('REDEEM', 'EXPIRED') AND lt.deletedAt IS NULL")
    Integer sumRedeemedPoints(@Param("tenantId") String tenantId, @Param("customerId") UUID customerId);

    /**
     * Find transactions within a date range for reporting.
     */
    Page<LoyaltyTransaction> findByTenantIdAndCreatedAtBetweenAndDeletedAtIsNull(
            String tenantId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Find transactions by reference (e.g., order ID).
     */
    Page<LoyaltyTransaction> findByTenantIdAndReferenceIdAndDeletedAtIsNull(
            String tenantId, UUID referenceId, Pageable pageable);
}
