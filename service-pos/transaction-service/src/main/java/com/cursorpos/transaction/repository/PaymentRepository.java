package com.cursorpos.transaction.repository;

import com.cursorpos.transaction.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    List<Payment> findByTenantIdAndTransactionIdAndDeletedAtIsNull(String tenantId, UUID transactionId);

    List<Payment> findByTenantIdAndPaymentMethodAndDeletedAtIsNull(String tenantId,
            Payment.PaymentMethod paymentMethod);
}
