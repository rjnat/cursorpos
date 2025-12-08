package com.cursorpos.admin.dto;

import com.cursorpos.admin.entity.LoyaltyTransaction.LoyaltyTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating loyalty transactions (earn/redeem/adjust points).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Transaction type is required")
    private LoyaltyTransactionType transactionType;

    @NotNull(message = "Points change is required")
    private Integer pointsChange;

    private UUID orderId;

    @NotBlank(message = "Description is required")
    private String description;
}
