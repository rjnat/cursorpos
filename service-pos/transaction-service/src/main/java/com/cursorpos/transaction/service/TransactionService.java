package com.cursorpos.transaction.service;

import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.PaymentRequest;
import com.cursorpos.transaction.dto.TransactionItemRequest;
import com.cursorpos.transaction.dto.TransactionRequest;
import com.cursorpos.transaction.dto.TransactionResponse;
import com.cursorpos.transaction.entity.Payment;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import com.cursorpos.transaction.mapper.TransactionMapper;
import com.cursorpos.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing transactions.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final String TRANSACTION_NOT_FOUND_MSG = "Transaction not found with ID: ";
    private static final String ENTITY_NAME = "transaction";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating transaction for tenant: {}", tenantId);

        Transaction transaction = transactionMapper.toTransaction(request);
        transaction.setTenantId(tenantId);
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Process items and calculate amounts
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (TransactionItemRequest itemRequest : request.getItems()) {
            TransactionItem item = transactionMapper.toTransactionItem(itemRequest);
            item.setTenantId(tenantId);

            // Calculate item amounts
            BigDecimal itemSubtotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            BigDecimal itemDiscount = itemRequest.getDiscountAmount() != null
                    ? itemRequest.getDiscountAmount()
                    : BigDecimal.ZERO;

            BigDecimal itemTaxableAmount = itemSubtotal.subtract(itemDiscount);

            BigDecimal itemTaxRate = itemRequest.getTaxRate() != null
                    ? itemRequest.getTaxRate()
                    : BigDecimal.ZERO;

            BigDecimal itemTax = itemTaxableAmount.multiply(itemTaxRate).divide(BigDecimal.valueOf(100));

            BigDecimal itemTotal = itemTaxableAmount.add(itemTax);

            item.setSubtotal(itemSubtotal);
            item.setDiscountAmount(itemDiscount);
            item.setTaxAmount(itemTax);
            item.setTotalAmount(itemTotal);

            transaction.addItem(item);

            subtotal = subtotal.add(itemSubtotal);
            totalTax = totalTax.add(itemTax);
        }

        BigDecimal transactionDiscount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal.subtract(transactionDiscount).add(totalTax);

        transaction.setSubtotal(subtotal);
        transaction.setTaxAmount(totalTax);
        transaction.setDiscountAmount(transactionDiscount);
        transaction.setTotalAmount(totalAmount);

        // Process payments
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PaymentRequest paymentRequest : request.getPayments()) {
            Payment payment = transactionMapper.toPayment(paymentRequest);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setTenantId(tenantId);
            transaction.addPayment(payment);
            totalPaid = totalPaid.add(paymentRequest.getAmount());
        }

        transaction.setPaidAmount(totalPaid);

        // Calculate change and update status based on payment
        if (totalPaid.compareTo(totalAmount) >= 0) {
            transaction.setChangeAmount(totalPaid.subtract(totalAmount));
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        } else {
            transaction.setChangeAmount(BigDecimal.ZERO);
            transaction.setStatus(Transaction.TransactionStatus.PENDING);
        }

        Objects.requireNonNull(transaction, ENTITY_NAME);
        Transaction saved = transactionRepository.save(transaction);

        log.info("Transaction created successfully with ID: {} and number: {}",
                saved.getId(), saved.getTransactionNumber());
        return transactionMapper.toTransactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        Transaction transaction = transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MSG + id));
        return transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByNumber(String transactionNumber) {
        String tenantId = TenantContext.getTenantId();
        Transaction transaction = transactionRepository
                .findByTenantIdAndTransactionNumberAndDeletedAtIsNull(tenantId, transactionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with number: " + transactionNumber));
        return transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getAllTransactions(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Transaction> page = transactionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(transactionMapper::toTransactionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionsByBranch(UUID branchId, Pageable pageable) {
        Objects.requireNonNull(branchId, "branchId");
        String tenantId = TenantContext.getTenantId();
        Page<Transaction> page = transactionRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(tenantId, branchId,
                pageable);
        return PagedResponse.of(page.map(transactionMapper::toTransactionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionsByCustomer(UUID customerId, Pageable pageable) {
        Objects.requireNonNull(customerId, "customerId");
        String tenantId = TenantContext.getTenantId();
        Page<Transaction> page = transactionRepository.findByTenantIdAndCustomerIdAndDeletedAtIsNull(tenantId,
                customerId, pageable);
        return PagedResponse.of(page.map(transactionMapper::toTransactionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionsByStatus(
            Transaction.TransactionStatus status, Pageable pageable) {
        Objects.requireNonNull(status, "status");
        String tenantId = TenantContext.getTenantId();
        Page<Transaction> page = transactionRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status,
                pageable);
        return PagedResponse.of(page.map(transactionMapper::toTransactionResponse));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        String tenantId = TenantContext.getTenantId();
        List<Transaction> transactions = transactionRepository.findByDateRange(tenantId, startDate, endDate);
        return transactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse cancelTransaction(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        log.info("Cancelling transaction with ID: {} for tenant: {}", id, tenantId);

        Transaction transaction = transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MSG + id));

        if (transaction.getStatus() == Transaction.TransactionStatus.CANCELLED) {
            throw new IllegalStateException("Transaction is already cancelled");
        }

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        Objects.requireNonNull(transaction, ENTITY_NAME);
        Transaction updated = transactionRepository.save(transaction);

        log.info("Transaction cancelled successfully with ID: {}", updated.getId());
        return transactionMapper.toTransactionResponse(updated);
    }

    private String generateTransactionNumber() {
        // Generate transaction number: TRX-YYYYMMDD-HHMMSS-UUID(8)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("TRX-%s-%s", timestamp, uuid);
    }
}
