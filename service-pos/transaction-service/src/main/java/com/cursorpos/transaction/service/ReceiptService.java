package com.cursorpos.transaction.service;

import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.ReceiptResponse;
import com.cursorpos.transaction.entity.Receipt;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.mapper.TransactionMapper;
import com.cursorpos.transaction.repository.ReceiptRepository;
import com.cursorpos.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing receipts.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private static final String RECEIPT_NOT_FOUND_MSG = "Receipt not found with ID: ";
    private static final String RECEIPT_BORDER = "========================================%n";

    private final ReceiptRepository receiptRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    @SuppressWarnings("null")
    public ReceiptResponse generateReceipt(UUID transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");
        String tenantId = TenantContext.getTenantId();
        log.info("Generating receipt for transaction ID: {}", transactionId);

        Transaction transaction = transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(transactionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        // Check if receipt already exists
        receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(tenantId, transactionId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Receipt already exists for this transaction");
                });

        Receipt receipt = Receipt.builder()
                .tenantId(tenantId)
                .transactionId(transactionId)
                .receiptNumber(generateReceiptNumber())
                .issuedDate(LocalDateTime.now())
                .receiptType("SALE")
                .content(generateReceiptContent(transaction))
                .printCount(0)
                .build();

        Receipt saved = receiptRepository.save(receipt);

        log.info("Receipt generated successfully with number: {}", saved.getReceiptNumber());
        return transactionMapper.toReceiptResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        Receipt receipt = receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(RECEIPT_NOT_FOUND_MSG + id));
        return transactionMapper.toReceiptResponse(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByTransaction(UUID transactionId) {
        Objects.requireNonNull(transactionId, "transactionId");
        String tenantId = TenantContext.getTenantId();
        Receipt receipt = receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(tenantId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receipt not found for transaction ID: " + transactionId));
        return transactionMapper.toReceiptResponse(receipt);
    }

    @Transactional
    public ReceiptResponse printReceipt(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        log.info("Printing receipt with ID: {}", id);

        Receipt receipt = receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(RECEIPT_NOT_FOUND_MSG + id));

        receipt.setPrintCount(receipt.getPrintCount() + 1);
        receipt.setLastPrintedAt(LocalDateTime.now());

        Receipt updated = receiptRepository.save(receipt);

        log.info("Receipt printed. Print count: {}", updated.getPrintCount());
        return transactionMapper.toReceiptResponse(updated);
    }

    private String generateReceiptNumber() {
        // Generate receipt number: RCP-YYYYMMDD-HHMMSS-UUID(8)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("RCP-%s-%s", timestamp, uuid);
    }

    private String generateReceiptContent(Transaction transaction) {
        // Simple text-based receipt content
        StringBuilder content = new StringBuilder();
        content.append(String.format(RECEIPT_BORDER));
        content.append(String.format("           SALES RECEIPT%n"));
        content.append(String.format(RECEIPT_BORDER));
        content.append(String.format("Transaction: %s%n", transaction.getTransactionNumber()));
        content.append(String.format("Date: %s%n",
                transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        content.append(String.format("Cashier: %s%n", transaction.getCashierName()));
        content.append(String.format(RECEIPT_BORDER + "%n"));

        content.append(String.format("ITEMS:%n"));
        transaction.getItems().forEach(item -> {
            content.append(String.format("%-20s x%d%n", item.getProductName(), item.getQuantity()));
            content.append(String.format("  @ %s = %s%n",
                    item.getUnitPrice(), item.getTotalAmount()));
        });

        content.append(String.format("%n" + RECEIPT_BORDER));
        content.append(String.format("Subtotal:     %s%n", transaction.getSubtotal()));
        content.append(String.format("Tax:          %s%n", transaction.getTaxAmount()));
        content.append(String.format("Discount:     %s%n", transaction.getDiscountAmount()));
        content.append(String.format("TOTAL:        %s%n", transaction.getTotalAmount()));
        content.append(String.format("Paid:         %s%n", transaction.getPaidAmount()));
        content.append(String.format("Change:       %s%n", transaction.getChangeAmount()));
        content.append(String.format(RECEIPT_BORDER));
        content.append(String.format("%n    Thank you for your purchase!%n"));
        content.append(String.format(RECEIPT_BORDER));

        return content.toString();
    }
}
