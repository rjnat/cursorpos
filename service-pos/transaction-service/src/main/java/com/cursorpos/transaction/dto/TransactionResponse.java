package com.cursorpos.transaction.dto;

import com.cursorpos.transaction.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for transactions.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;
    private String transactionNumber;
    private UUID branchId;
    private UUID customerId;
    private LocalDateTime transactionDate;
    private Transaction.TransactionStatus status;
    private Transaction.TransactionType type;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String notes;
    private UUID cashierId;
    private String cashierName;
    
    @Builder.Default
    private List<TransactionItemResponse> items = new ArrayList<>();
    
    @Builder.Default
    private List<PaymentResponse> payments = new ArrayList<>();
    
    private Instant createdAt;
    private Instant updatedAt;
}
