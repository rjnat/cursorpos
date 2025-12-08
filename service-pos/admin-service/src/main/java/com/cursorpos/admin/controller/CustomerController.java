package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.service.CustomerService;
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
 * REST controller for customer management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Customer created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByCode(@PathVariable String code) {
        CustomerResponse response = customerService.getCustomerByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByEmail(@PathVariable String email) {
        CustomerResponse response = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByPhone(@PathVariable String phone) {
        CustomerResponse response = customerService.getCustomerByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(Pageable pageable) {
        PagedResponse<CustomerResponse> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/loyalty-tier/{tierId}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getCustomersByLoyaltyTier(
            @PathVariable UUID tierId, Pageable pageable) {
        PagedResponse<CustomerResponse> response = customerService.getCustomersByLoyaltyTier(tierId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> activateCustomer(@PathVariable UUID id) {
        CustomerResponse response = customerService.activateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> deactivateCustomer(@PathVariable UUID id) {
        CustomerResponse response = customerService.deactivateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer deactivated successfully"));
    }

    @PostMapping("/{id}/loyalty-points")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> addLoyaltyPoints(
            @PathVariable UUID id,
            @RequestParam Integer points) {
        CustomerResponse response = customerService.addLoyaltyPoints(id, points);
        return ResponseEntity.ok(ApiResponse.success(response, "Loyalty points added successfully"));
    }
}
