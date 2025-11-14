package com.cursorpos.transaction.repository;

import com.cursorpos.transaction.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TransactionItem entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, UUID> {

    Optional<TransactionItem> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    List<TransactionItem> findByTenantIdAndTransactionIdAndDeletedAtIsNull(String tenantId, UUID transactionId);

    List<TransactionItem> findByTenantIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID productId);
}
