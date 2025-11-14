package com.cursorpos.transaction.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * TransactionItem entity representing an item in a transaction.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Entity
@Table(name = "transaction_items", indexes = {
        @Index(name = "idx_transaction_item_tenant", columnList = "tenant_id"),
        @Index(name = "idx_transaction_item_transaction", columnList = "tenant_id,transaction_id"),
        @Index(name = "idx_transaction_item_product", columnList = "tenant_id,product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TransactionItem extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;
}
