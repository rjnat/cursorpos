package com.cursorpos.transaction.dto;

import com.cursorpos.transaction.entity.Transaction.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    private UUID customerId;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotEmpty(message = "Transaction must have at least one item")
    @Valid
    private List<TransactionItemDto> items;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotEmpty(message = "Transaction must have at least one payment")
    @Valid
    private List<PaymentDto> payments;

    private String notes;

    private UUID cashierId;

    private String cashierName;
}
