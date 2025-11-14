package com.cursorpos.product.service;

import com.cursorpos.product.dto.PriceHistoryResponse;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.PriceHistoryRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing price history.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PagedResponse<PriceHistoryResponse> getPriceHistory(UUID productId, Pageable pageable) {
        Objects.requireNonNull(productId, "productId");
        String tenantId = TenantContext.getTenantId();
        Page<PriceHistory> page = priceHistoryRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId,
                productId, pageable);
        return PagedResponse.of(page.map(productMapper::toPriceHistoryResponse));
    }

    @Transactional(readOnly = true)
    public Optional<PriceHistoryResponse> getEffectivePrice(UUID productId, LocalDateTime date) {
        Objects.requireNonNull(productId, "productId");
        String tenantId = TenantContext.getTenantId();
        LocalDateTime effectiveDate = date != null ? date : LocalDateTime.now();

        return priceHistoryRepository.findEffectivePrice(tenantId, productId, effectiveDate)
                .map(productMapper::toPriceHistoryResponse);
    }
}
