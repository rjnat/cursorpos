package com.cursorpos.product.controller;

import com.cursorpos.product.dto.PriceHistoryResponse;
import com.cursorpos.product.service.PriceHistoryService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for price history.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/api/v1/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PagedResponse<PriceHistoryResponse>>> getPriceHistory(
            @PathVariable UUID productId,
            Pageable pageable) {
        PagedResponse<PriceHistoryResponse> response = priceHistoryService.getPriceHistory(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/product/{productId}/effective")
    public ResponseEntity<ApiResponse<PriceHistoryResponse>> getEffectivePrice(
            @PathVariable UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return priceHistoryService.getEffectivePrice(productId, date)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.notFound().build());
    }
}
