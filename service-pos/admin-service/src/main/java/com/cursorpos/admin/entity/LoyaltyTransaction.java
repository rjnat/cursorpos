package com.cursorpos.admin.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Loyalty transaction entity representing point earn/redeem history.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Entity
@Table(name = "loyalty_transactions", indexes = {
        @Index(name = "idx_loyalty_transactions_tenant_customer", columnList = "tenant_id, customer_id"),
        @Index(name = "idx_loyalty_transactions_type", columnList = "tenant_id, transaction_type"),
        @Index(name = "idx_loyalty_transactions_created_at", columnList = "tenant_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private LoyaltyTransactionType transactionType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Loyalty transaction types.
     */
    public enum LoyaltyTransactionType {
        EARN, // Points earned from purchase
        REDEEM, // Points redeemed for discount
        ADJUSTMENT, // Manual adjustment by admin
        EXPIRED, // Points expired
        BONUS, // Bonus points (promotion)
        REFUND // Points refunded due to return
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LoyaltyTransaction that = (LoyaltyTransaction) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
