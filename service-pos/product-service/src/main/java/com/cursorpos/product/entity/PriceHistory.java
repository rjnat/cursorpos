package com.cursorpos.product.entity;

import com.cursorpos.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Price History entity for tracking product price changes.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Entity
@Table(name = "price_history", indexes = {
    @Index(name = "idx_price_history_tenant", columnList = "tenant_id"),
    @Index(name = "idx_price_history_product", columnList = "tenant_id,product_id"),
    @Index(name = "idx_price_history_effective", columnList = "tenant_id,product_id,effective_from")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PriceHistory extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_price", precision = 19, scale = 4)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal newPrice;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(length = 500)
    private String reason;
}
