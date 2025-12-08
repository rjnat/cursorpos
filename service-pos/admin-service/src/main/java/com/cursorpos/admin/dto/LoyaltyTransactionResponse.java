package com.cursorpos.admin.dto;

import com.cursorpos.admin.entity.LoyaltyTransaction.LoyaltyTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for loyalty transaction response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionResponse {

    private UUID id;
    private UUID customerId;
    private LoyaltyTransactionType transactionType;
    private Integer points;
    private Integer balanceAfter;
    private UUID referenceId;
    private String referenceType;
    private String description;
    private Instant createdAt;
}
