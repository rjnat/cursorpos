package com.cursorpos.transaction.mapper;

import com.cursorpos.transaction.dto.*;
import com.cursorpos.transaction.entity.Payment;
import com.cursorpos.transaction.entity.Receipt;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import org.mapstruct.*;

/**
 * MapStruct mapper for Transaction domain entities and DTOs.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    // Transaction mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transactionNumber", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "changeAmount", ignore = true)
    Transaction toTransaction(TransactionRequest request);

    TransactionResponse toTransactionResponse(Transaction transaction);

    // TransactionItem mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    TransactionItem toTransactionItem(TransactionItemRequest request);

    TransactionItemResponse toTransactionItemResponse(TransactionItem item);

    // Payment mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    Payment toPayment(PaymentRequest request);

    PaymentResponse toPaymentResponse(Payment payment);

    // Receipt mappings
    ReceiptResponse toReceiptResponse(Receipt receipt);
}
