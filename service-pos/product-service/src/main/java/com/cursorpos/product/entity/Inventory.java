package com.cursorpos.product.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Inventory entity for tracking product stock levels per branch.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inventory_product", columnList = "tenant_id,product_id"),
        @Index(name = "idx_inventory_branch", columnList = "tenant_id,branch_id"),
        @Index(name = "idx_inventory_product_branch", columnList = "tenant_id,product_id,branch_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Inventory extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    /**
     * Calculate available quantity.
     */
    @PreUpdate
    @PrePersist
    public void calculateAvailableQuantity() {
        this.quantityAvailable = this.quantityOnHand - this.quantityReserved;
    }
}
