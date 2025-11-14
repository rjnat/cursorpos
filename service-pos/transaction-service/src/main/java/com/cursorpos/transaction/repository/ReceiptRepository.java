package com.cursorpos.transaction.repository;

import com.cursorpos.transaction.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Receipt entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Optional<Receipt> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Optional<Receipt> findByTenantIdAndTransactionIdAndDeletedAtIsNull(String tenantId, UUID transactionId);

    Optional<Receipt> findByTenantIdAndReceiptNumberAndDeletedAtIsNull(String tenantId, String receiptNumber);
}
