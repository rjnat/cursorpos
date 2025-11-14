package com.cursorpos.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for receipts.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptResponse {

    private UUID id;
    private UUID transactionId;
    private String receiptNumber;
    private LocalDateTime issuedDate;
    private String receiptType;
    private String content;
    private Integer printCount;
    private LocalDateTime lastPrintedAt;
}
