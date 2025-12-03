package com.cursorpos.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for transaction item details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemDto {

    private UUID id;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private String productCode;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than zero")
    private BigDecimal unitPrice;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private String notes;

    private LocalDateTime createdAt;
    private String createdBy;
}
