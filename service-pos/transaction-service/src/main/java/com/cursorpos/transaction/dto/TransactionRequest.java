package com.cursorpos.transaction.dto;

import com.cursorpos.transaction.entity.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating/updating transactions.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    private UUID customerId;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;

    @NotEmpty(message = "At least one item is required")
    @Valid
    @Builder.Default
    private List<TransactionItemRequest> items = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<PaymentRequest> payments = new ArrayList<>();

    private BigDecimal discountAmount;

    private String notes;

    private UUID cashierId;

    private String cashierName;
}
