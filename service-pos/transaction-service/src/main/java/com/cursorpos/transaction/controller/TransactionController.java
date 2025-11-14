package com.cursorpos.transaction.controller;

import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.transaction.dto.TransactionRequest;
import com.cursorpos.transaction.dto.TransactionResponse;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for transaction operations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.createTransaction(request), "Transaction created successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable UUID id) {
        return ApiResponse.success(transactionService.getTransactionById(id));
    }

    @GetMapping("/number/{transactionNumber}")
    public ApiResponse<TransactionResponse> getTransactionByNumber(@PathVariable String transactionNumber) {
        return ApiResponse.success(transactionService.getTransactionByNumber(transactionNumber));
    }

    @GetMapping
    public PagedResponse<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionService.getAllTransactions(pageable);
    }

    @GetMapping("/branch/{branchId}")
    public PagedResponse<TransactionResponse> getTransactionsByBranch(
            @PathVariable UUID branchId,
            Pageable pageable) {
        return transactionService.getTransactionsByBranch(branchId, pageable);
    }

    @GetMapping("/customer/{customerId}")
    public PagedResponse<TransactionResponse> getTransactionsByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        return transactionService.getTransactionsByCustomer(customerId, pageable);
    }

    @GetMapping("/status/{status}")
    public PagedResponse<TransactionResponse> getTransactionsByStatus(
            @PathVariable Transaction.TransactionStatus status,
            Pageable pageable) {
        return transactionService.getTransactionsByStatus(status, pageable);
    }

    @GetMapping("/date-range")
    public ApiResponse<List<TransactionResponse>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.success(transactionService.getTransactionsByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<TransactionResponse> cancelTransaction(@PathVariable UUID id) {
        return ApiResponse.success(transactionService.cancelTransaction(id), "Transaction cancelled successfully");
    }
}
