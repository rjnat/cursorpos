package com.cursorpos.product.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Stock movement entity for tracking inventory changes.
 * Provides audit trail for all stock-related transactions.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_movements_tenant_store", columnList = "tenant_id,store_id"),
        @Index(name = "idx_stock_movements_product", columnList = "product_id"),
        @Index(name = "idx_stock_movements_reference", columnList = "reference_number"),
        @Index(name = "idx_stock_movements_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "reference_order_id")
    private UUID referenceOrderId;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Type of stock movement.
     */
    public enum MovementType {
        /** Initial stock setup */
        RESTOCK,
        /** Purchase from supplier */
        PURCHASE,
        /** Sale to customer */
        SALE,
        /** Return from customer */
        RETURN,
        /** Transfer to another store */
        TRANSFER_OUT,
        /** Transfer from another store */
        TRANSFER_IN,
        /** Adjustment for damaged items */
        DAMAGE,
        /** Adjustment for theft/loss */
        LOSS,
        /** Manual adjustment */
        ADJUSTMENT,
        /** Reservation for pending order */
        RESERVE,
        /** Release of reservation */
        RELEASE,
        /** Count correction after physical inventory */
        INVENTORY_COUNT
    }

    /**
     * Checks if this movement increases stock.
     */
    public boolean isIncrease() {
        return quantityDelta > 0;
    }

    /**
     * Checks if this movement decreases stock.
     */
    public boolean isDecrease() {
        return quantityDelta < 0;
    }

    /**
     * Gets the absolute change in quantity.
     */
    public Integer getAbsoluteChange() {
        return Math.abs(quantityDelta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StockMovement))
            return false;
        if (!super.equals(o))
            return false;

        StockMovement that = (StockMovement) o;

        if (!storeId.equals(that.storeId))
            return false;
        if (!product.equals(that.product))
            return false;
        return movementType == that.movementType;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + storeId.hashCode();
        result = 31 * result + product.hashCode();
        result = 31 * result + movementType.hashCode();
        return result;
    }
}
