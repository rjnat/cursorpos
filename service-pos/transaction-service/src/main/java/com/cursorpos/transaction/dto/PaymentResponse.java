package com.cursorpos.transaction.dto;

import com.cursorpos.transaction.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for payments.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private UUID id;
    private Payment.PaymentMethod paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String referenceNumber;
    private String notes;
}
