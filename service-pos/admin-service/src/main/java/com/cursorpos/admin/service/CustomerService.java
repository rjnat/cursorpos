package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.CustomerRepository;
import com.cursorpos.admin.repository.LoyaltyTierRepository;
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
 * Service for managing customers.
 * Customers are tenant-wide (shared across all stores).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private static final String CUSTOMER_NOT_FOUND_MSG = "Customer not found with ID: ";

    private final CustomerRepository customerRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating customer with code: {} for tenant: {}", request.getCode(), tenantId);

        if (customerRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Customer with code " + request.getCode() + " already exists");
        }

        Customer customer = adminMapper.toCustomer(request);
        customer.setTenantId(tenantId);

        // Assign default (lowest) loyalty tier
        loyaltyTierRepository.findTierForPoints(tenantId, 0)
                .ifPresent(tier -> customer.setLoyaltyTierId(tier.getId()));

        Customer saved = customerRepository.save(customer);

        log.info("Customer created successfully with ID: {}", saved.getId());
        return adminMapper.toCustomerResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));
        return adminMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + code));
        return adminMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Customer> page = customerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(adminMapper::toCustomerResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getCustomersByLoyaltyTier(UUID loyaltyTierId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Customer> page = customerRepository.findByTenantIdAndLoyaltyTierIdAndDeletedAtIsNull(
                tenantId, loyaltyTierId, pageable);
        return PagedResponse.of(page.map(adminMapper::toCustomerResponse));
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, CreateCustomerRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating customer with ID: {} for tenant: {}", id, tenantId);
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");

        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));

        adminMapper.updateCustomerFromRequest(request, customer);
        Objects.requireNonNull(customer, "customer");
        Customer updated = customerRepository.save(customer);

        log.info("Customer updated successfully with ID: {}", updated.getId());
        return adminMapper.toCustomerResponse(updated);
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting customer with ID: {} for tenant: {}", id, tenantId);
        Objects.requireNonNull(id, "id");

        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));

        customer.softDelete();
        customerRepository.save(customer);

        log.info("Customer soft-deleted successfully with ID: {}", id);
    }

    @Transactional
    public CustomerResponse activateCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));
        customer.setIsActive(true);
        Customer updated = customerRepository.save(customer);
        return adminMapper.toCustomerResponse(updated);
    }

    @Transactional
    public CustomerResponse deactivateCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));
        customer.setIsActive(false);
        Customer updated = customerRepository.save(customer);
        return adminMapper.toCustomerResponse(updated);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByTenantIdAndEmailAndDeletedAtIsNull(tenantId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return adminMapper.toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByTenantIdAndPhoneAndDeletedAtIsNull(tenantId, phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
        return adminMapper.toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse addLoyaltyPoints(UUID id, Integer points) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(points, "points");
        log.info("Adding {} loyalty points to customer with ID: {} for tenant: {}", points, id, tenantId);

        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + id));

        int newTotalPoints = (customer.getTotalPoints() != null ? customer.getTotalPoints() : 0) + points;
        int newAvailablePoints = (customer.getAvailablePoints() != null ? customer.getAvailablePoints() : 0) + points;
        int newLifetimePoints = (customer.getLifetimePoints() != null ? customer.getLifetimePoints() : 0) + points;

        customer.setTotalPoints(newTotalPoints);
        customer.setAvailablePoints(newAvailablePoints);
        customer.setLifetimePoints(newLifetimePoints);

        // Check if customer should be upgraded to a new tier
        loyaltyTierRepository.findTierForPoints(tenantId, newTotalPoints)
                .ifPresent(tier -> customer.setLoyaltyTierId(tier.getId()));

        Customer updated = customerRepository.save(customer);
        log.info("Loyalty points updated to {} for customer with ID: {}", newTotalPoints, id);
        return adminMapper.toCustomerResponse(updated);
    }
}
