package com.cursorpos.transaction.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Transaction entity representing a sales transaction.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_tenant", columnList = "tenant_id"),
        @Index(name = "idx_transaction_branch", columnList = "tenant_id,branch_id"),
        @Index(name = "idx_transaction_customer", columnList = "tenant_id,customer_id"),
        @Index(name = "idx_transaction_date", columnList = "tenant_id,transaction_date"),
        @Index(name = "idx_transaction_status", columnList = "tenant_id,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "transaction_number", nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 19, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "change_amount", precision = 19, scale = 4)
    private BigDecimal changeAmount;

    @Column(length = 500)
    private String notes;

    @Column(name = "cashier_id")
    private UUID cashierId;

    @Column(name = "cashier_name", length = 100)
    private String cashierName;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        REFUNDED
    }

    public enum TransactionType {
        SALE,
        RETURN,
        EXCHANGE
    }

    public void addItem(TransactionItem item) {
        items.add(item);
        item.setTransaction(this);
    }

    public void removeItem(TransactionItem item) {
        items.remove(item);
        item.setTransaction(null);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setTransaction(this);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setTransaction(null);
    }
}
