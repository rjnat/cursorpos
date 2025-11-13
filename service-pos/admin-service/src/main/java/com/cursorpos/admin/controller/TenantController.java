package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.service.TenantService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for tenant management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasAuthority('TENANT_CREATE')")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tenant created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TENANT_READ')")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('TENANT_READ')")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantByCode(@PathVariable String code) {
        TenantResponse response = tenantService.getTenantByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TENANT_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<TenantResponse>>> getAllTenants(Pageable pageable) {
        PagedResponse<TenantResponse> response = tenantService.getAllTenants(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TENANT_UPDATE')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tenant updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TENANT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('TENANT_UPDATE')")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.activateTenant(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Tenant activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('TENANT_UPDATE')")
    public ResponseEntity<ApiResponse<TenantResponse>> deactivateTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.deactivateTenant(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Tenant deactivated successfully"));
    }
}
