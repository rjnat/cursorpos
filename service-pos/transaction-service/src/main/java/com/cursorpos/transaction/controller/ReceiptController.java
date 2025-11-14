package com.cursorpos.transaction.controller;

import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.transaction.dto.ReceiptResponse;
import com.cursorpos.transaction.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for receipt operations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/transaction/{transactionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReceiptResponse> generateReceipt(@PathVariable UUID transactionId) {
        return ApiResponse.success(receiptService.generateReceipt(transactionId), "Receipt generated successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<ReceiptResponse> getReceiptById(@PathVariable UUID id) {
        return ApiResponse.success(receiptService.getReceiptById(id));
    }

    @GetMapping("/transaction/{transactionId}")
    public ApiResponse<ReceiptResponse> getReceiptByTransaction(@PathVariable UUID transactionId) {
        return ApiResponse.success(receiptService.getReceiptByTransaction(transactionId));
    }

    @PutMapping("/{id}/print")
    public ApiResponse<ReceiptResponse> printReceipt(@PathVariable UUID id) {
        return ApiResponse.success(receiptService.printReceipt(id), "Receipt printed successfully");
    }
}
