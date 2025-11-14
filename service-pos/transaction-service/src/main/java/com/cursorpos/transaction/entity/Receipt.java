package com.cursorpos.transaction.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Receipt entity representing a printed/generated receipt.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Entity
@Table(name = "receipts", indexes = {
        @Index(name = "idx_receipt_tenant", columnList = "tenant_id"),
        @Index(name = "idx_receipt_transaction", columnList = "tenant_id,transaction_id"),
        @Index(name = "idx_receipt_number", columnList = "tenant_id,receipt_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Receipt extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "receipt_type", length = 20)
    private String receiptType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "print_count")
    @Builder.Default
    private Integer printCount = 0;

    @Column(name = "last_printed_at")
    private LocalDateTime lastPrintedAt;
}
