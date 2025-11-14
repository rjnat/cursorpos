package com.cursorpos.transaction.repository;

import com.cursorpos.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Optional<Transaction> findByTenantIdAndTransactionNumberAndDeletedAtIsNull(String tenantId,
            String transactionNumber);

    Page<Transaction> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Transaction> findByTenantIdAndBranchIdAndDeletedAtIsNull(String tenantId, UUID branchId, Pageable pageable);

    Page<Transaction> findByTenantIdAndCustomerIdAndDeletedAtIsNull(String tenantId, UUID customerId,
            Pageable pageable);

    Page<Transaction> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, Transaction.TransactionStatus status,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.tenantId = :tenantId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "AND t.deletedAt IS NULL")
    List<Transaction> findByDateRange(@Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.tenantId = :tenantId " +
            "AND t.branchId = :branchId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "AND t.deletedAt IS NULL")
    List<Transaction> findByBranchAndDateRange(@Param("tenantId") String tenantId,
            @Param("branchId") UUID branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
