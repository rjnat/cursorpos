package com.cursorpos.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for transaction items.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private String productCode;

    @NotNull(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private BigDecimal taxRate;

    private String notes;
}
