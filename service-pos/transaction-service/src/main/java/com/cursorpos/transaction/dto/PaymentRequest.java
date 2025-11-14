package com.cursorpos.transaction.dto;

import com.cursorpos.transaction.entity.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for payments.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String referenceNumber;

    private String notes;
}
