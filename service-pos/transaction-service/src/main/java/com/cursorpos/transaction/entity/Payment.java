package com.cursorpos.transaction.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing a payment for a transaction.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_tenant", columnList = "tenant_id"),
        @Index(name = "idx_payment_transaction", columnList = "tenant_id,transaction_id"),
        @Index(name = "idx_payment_date", columnList = "tenant_id,payment_date"),
        @Index(name = "idx_payment_method", columnList = "tenant_id,payment_method")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 500)
    private String notes;

    public enum PaymentMethod {
        CASH,
        CREDIT_CARD,
        DEBIT_CARD,
        E_WALLET,
        BANK_TRANSFER,
        CHECK
    }
}
