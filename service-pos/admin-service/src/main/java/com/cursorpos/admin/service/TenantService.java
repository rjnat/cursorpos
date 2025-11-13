package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.entity.Tenant;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.TenantRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing tenants.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("Creating tenant with code: {}", request.getCode());

        if (tenantRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Tenant with code " + request.getCode() + " already exists");
        }

        if (request.getSubdomain() != null && tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new IllegalArgumentException("Tenant with subdomain " + request.getSubdomain() + " already exists");
        }

        Tenant tenant = adminMapper.toTenant(request);
        tenant.setTenantId(tenant.getCode());
        Tenant saved = tenantRepository.save(tenant);

        log.info("Tenant created successfully with ID: {}", saved.getId());
        return adminMapper.toTenantResponse(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));
        return adminMapper.toTenantResponse(tenant);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantByCode(String code) {
        Tenant tenant = tenantRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with code: " + code));
        return adminMapper.toTenantResponse(tenant);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TenantResponse> getAllTenants(Pageable pageable) {
        Page<Tenant> page = tenantRepository.findByDeletedAtIsNull(pageable);
        return PagedResponse.of(page.map(adminMapper::toTenantResponse));
    }

    @Transactional
    public TenantResponse updateTenant(UUID id, CreateTenantRequest request) {
        log.info("Updating tenant with ID: {}", id);

        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        adminMapper.updateTenantFromRequest(request, tenant);
        Tenant updated = tenantRepository.save(tenant);

        log.info("Tenant updated successfully with ID: {}", updated.getId());
        return adminMapper.toTenantResponse(updated);
    }

    @Transactional
    public void deleteTenant(UUID id) {
        log.info("Deleting tenant with ID: {}", id);

        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        tenant.softDelete();
        tenantRepository.save(tenant);

        log.info("Tenant soft-deleted successfully with ID: {}", id);
    }

    @Transactional
    public TenantResponse activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        tenant.setIsActive(true);
        Tenant updated = tenantRepository.save(tenant);
        return adminMapper.toTenantResponse(updated);
    }

    @Transactional
    public TenantResponse deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + id));

        tenant.setIsActive(false);
        Tenant updated = tenantRepository.save(tenant);
        return adminMapper.toTenantResponse(updated);
    }
}
