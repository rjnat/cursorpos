package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.entity.Store;
import com.cursorpos.admin.mapper.AdminMapper;
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

import java.util.UUID;
import java.util.Objects;

/**
 * Service for managing stores.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private static final String STORE_NOT_FOUND_MSG = "Store not found with ID: ";

    private final StoreRepository storeRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating store with code: {} for tenant: {}", request.getCode(), tenantId);

        if (storeRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Store with code " + request.getCode() + " already exists");
        }

        Store store = adminMapper.toStore(request);
        store.setTenantId(tenantId);
        Store saved = storeRepository.save(store);

        log.info("Store created successfully with ID: {}", saved.getId());
        return adminMapper.toStoreResponse(saved);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Store store = storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_NOT_FOUND_MSG + id));
        return adminMapper.toStoreResponse(store);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        Store store = storeRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with code: " + code));
        return adminMapper.toStoreResponse(store);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoreResponse> getAllStores(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Store> page = storeRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(adminMapper::toStoreResponse));
    }

    @Transactional
    public StoreResponse updateStore(UUID id, CreateStoreRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating store with ID: {} for tenant: {}", id, tenantId);

        Store store = storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_NOT_FOUND_MSG + id));

        adminMapper.updateStoreFromRequest(request, store);
        Objects.requireNonNull(store, "store");
        Store updated = storeRepository.save(store);

        log.info("Store updated successfully with ID: {}", updated.getId());
        return adminMapper.toStoreResponse(updated);
    }

    @Transactional
    public void deleteStore(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting store with ID: {} for tenant: {}", id, tenantId);

        Store store = storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_NOT_FOUND_MSG + id));

        store.softDelete();
        storeRepository.save(store);

        log.info("Store soft-deleted successfully with ID: {}", id);
    }
}
