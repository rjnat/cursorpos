package com.cursorpos.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for transaction items.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemResponse {

    private UUID id;
    private UUID productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String notes;
}
