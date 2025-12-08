package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.entity.StorePriceOverride;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.StorePriceOverrideRepository;
import com.cursorpos.admin.repository.StoreRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing store-specific price overrides.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorePriceOverrideService {

    private static final String PARAM_STORE_ID = "storeId";

    private static final String OVERRIDE_NOT_FOUND_MSG = "Price override not found with ID: ";

    private final StorePriceOverrideRepository priceOverrideRepository;
    private final StoreRepository storeRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public StorePriceOverrideResponse createOverride(StorePriceOverrideRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating price override for store: {} product: {}", request.getStoreId(), request.getProductId());

        // Validate store exists
        storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getStoreId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + request.getStoreId()));

        // Check if override already exists
        if (priceOverrideRepository.existsByTenantIdAndStoreIdAndProductIdAndDeletedAtIsNull(
                tenantId, request.getStoreId(), request.getProductId())) {
            throw new IllegalArgumentException("Price override already exists for this store and product");
        }

        StorePriceOverride override = adminMapper.toStorePriceOverride(request);
        override.setTenantId(tenantId);
        StorePriceOverride saved = priceOverrideRepository.save(override);

        log.info("Price override created successfully with ID: {}", saved.getId());
        return adminMapper.toStorePriceOverrideResponse(saved);
    }

    @Transactional(readOnly = true)
    public StorePriceOverrideResponse getOverrideById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        StorePriceOverride override = priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND_MSG + id));
        return adminMapper.toStorePriceOverrideResponse(override);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StorePriceOverrideResponse> getOverridesByStore(UUID storeId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(storeId, PARAM_STORE_ID);
        Page<StorePriceOverride> page = priceOverrideRepository
                .findByTenantIdAndStoreIdAndDeletedAtIsNull(tenantId, storeId, pageable);
        return PagedResponse.of(page.map(adminMapper::toStorePriceOverrideResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<StorePriceOverrideResponse> getOverridesByProduct(UUID productId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(productId, "productId");
        Page<StorePriceOverride> page = priceOverrideRepository
                .findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId, pageable);
        return PagedResponse.of(page.map(adminMapper::toStorePriceOverrideResponse));
    }

    @Transactional(readOnly = true)
    public Optional<StorePriceOverrideResponse> getActiveOverride(UUID storeId, UUID productId) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(storeId, PARAM_STORE_ID);
        Objects.requireNonNull(productId, "productId");
        return priceOverrideRepository.findActiveOverride(tenantId, storeId, productId, Instant.now())
                .map(adminMapper::toStorePriceOverrideResponse);
    }

    @Transactional(readOnly = true)
    public List<StorePriceOverrideResponse> getAllActiveOverridesForStore(UUID storeId) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(storeId, PARAM_STORE_ID);
        List<StorePriceOverride> overrides = priceOverrideRepository
                .findAllActiveOverridesForStore(tenantId, storeId, Instant.now());
        return overrides.stream()
                .map(adminMapper::toStorePriceOverrideResponse)
                .toList();
    }

    @Transactional
    public StorePriceOverrideResponse updateOverride(UUID id, StorePriceOverrideRequest request) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        log.info("Updating price override with ID: {}", id);

        StorePriceOverride override = priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND_MSG + id));

        adminMapper.updateStorePriceOverrideFromRequest(request, override);
        @SuppressWarnings("null") // JPA save() never returns null
        StorePriceOverride updated = priceOverrideRepository.save(override);

        log.info("Price override updated successfully with ID: {}", updated.getId());
        return adminMapper.toStorePriceOverrideResponse(updated);
    }

    @Transactional
    public void deleteOverride(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        log.info("Deleting price override with ID: {}", id);

        StorePriceOverride override = priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND_MSG + id));

        override.softDelete();
        priceOverrideRepository.save(override);

        log.info("Price override soft-deleted successfully with ID: {}", id);
    }

    @Transactional
    public StorePriceOverrideResponse activateOverride(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        StorePriceOverride override = priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND_MSG + id));
        override.setIsActive(true);
        StorePriceOverride updated = priceOverrideRepository.save(override);
        return adminMapper.toStorePriceOverrideResponse(updated);
    }

    @Transactional
    public StorePriceOverrideResponse deactivateOverride(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        StorePriceOverride override = priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(OVERRIDE_NOT_FOUND_MSG + id));
        override.setIsActive(false);
        StorePriceOverride updated = priceOverrideRepository.save(override);
        return adminMapper.toStorePriceOverrideResponse(updated);
    }
}
