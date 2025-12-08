package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.LoyaltyTierRequest;
import com.cursorpos.admin.dto.LoyaltyTierResponse;
import com.cursorpos.admin.dto.LoyaltyTransactionRequest;
import com.cursorpos.admin.dto.LoyaltyTransactionResponse;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.entity.LoyaltyTier;
import com.cursorpos.admin.entity.LoyaltyTransaction;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.CustomerRepository;
import com.cursorpos.admin.repository.LoyaltyTierRepository;
import com.cursorpos.admin.repository.LoyaltyTransactionRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing loyalty program (tiers and transactions).
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private static final String PARAM_REQUEST = "request";

    private static final String TIER_NOT_FOUND_MSG = "Loyalty tier not found with ID: ";
    private static final String CUSTOMER_NOT_FOUND_MSG = "Customer not found with ID: ";

    private final LoyaltyTierRepository loyaltyTierRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final CustomerRepository customerRepository;
    private final AdminMapper adminMapper;

    // ========== Loyalty Tier Management ==========

    @Transactional
    public LoyaltyTierResponse createTier(LoyaltyTierRequest request) {
        Objects.requireNonNull(request, PARAM_REQUEST);
        String tenantId = TenantContext.getTenantId();
        log.info("Creating loyalty tier with code: {} for tenant: {}", request.getCode(), tenantId);

        if (loyaltyTierRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Tier with code " + request.getCode() + " already exists");
        }

        LoyaltyTier tier = adminMapper.toLoyaltyTier(request);
        tier.setTenantId(tenantId);
        LoyaltyTier saved = loyaltyTierRepository.save(tier);

        log.info("Loyalty tier created successfully with ID: {}", saved.getId());
        return adminMapper.toLoyaltyTierResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoyaltyTierResponse getTierById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        LoyaltyTier tier = loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(TIER_NOT_FOUND_MSG + id));
        return adminMapper.toLoyaltyTierResponse(tier);
    }

    @Transactional(readOnly = true)
    public LoyaltyTierResponse getTierByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        LoyaltyTier tier = loyaltyTierRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found with code: " + code));
        return adminMapper.toLoyaltyTierResponse(tier);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTierResponse> getAllTiers(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<LoyaltyTier> page = loyaltyTierRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(adminMapper::toLoyaltyTierResponse));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTierResponse> getAllTiersOrdered() {
        String tenantId = TenantContext.getTenantId();
        List<LoyaltyTier> tiers = loyaltyTierRepository.findByTenantIdAndDeletedAtIsNullOrderByMinPointsAsc(tenantId);
        return tiers.stream()
                .map(adminMapper::toLoyaltyTierResponse)
                .toList();
    }

    @Transactional
    public LoyaltyTierResponse updateTier(UUID id, LoyaltyTierRequest request) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, PARAM_REQUEST);
        log.info("Updating loyalty tier with ID: {} for tenant: {}", id, tenantId);

        LoyaltyTier tier = loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(TIER_NOT_FOUND_MSG + id));

        adminMapper.updateLoyaltyTierFromRequest(request, tier);
        @SuppressWarnings("null") // JPA save() never returns null
        LoyaltyTier updated = loyaltyTierRepository.save(tier);

        log.info("Loyalty tier updated successfully with ID: {}", updated.getId());
        return adminMapper.toLoyaltyTierResponse(updated);
    }

    @Transactional
    public void deleteTier(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        log.info("Deleting loyalty tier with ID: {} for tenant: {}", id, tenantId);

        LoyaltyTier tier = loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(TIER_NOT_FOUND_MSG + id));

        tier.softDelete();
        loyaltyTierRepository.save(tier);

        log.info("Loyalty tier soft-deleted successfully with ID: {}", id);
    }

    // ========== Loyalty Transaction Management ==========

    @Transactional
    public LoyaltyTransactionResponse createTransaction(LoyaltyTransactionRequest request) {
        Objects.requireNonNull(request, PARAM_REQUEST);
        String tenantId = TenantContext.getTenantId();
        log.info("Creating loyalty transaction for customer: {} type: {}",
                request.getCustomerId(), request.getTransactionType());

        // Validate customer exists
        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getCustomerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + request.getCustomerId()));

        // Calculate new balance
        int currentPoints = customer.getAvailablePoints() != null ? customer.getAvailablePoints() : 0;
        int newBalance = currentPoints + request.getPointsChange();

        if (newBalance < 0) {
            throw new IllegalArgumentException("Insufficient points. Current: " + currentPoints +
                    ", Requested: " + request.getPointsChange());
        }

        // Create transaction
        LoyaltyTransaction transaction = adminMapper.toLoyaltyTransaction(request);
        transaction.setTenantId(tenantId);
        transaction.setBalanceAfter(newBalance);
        LoyaltyTransaction saved = loyaltyTransactionRepository.save(transaction);

        // Update customer points
        customer.setAvailablePoints(newBalance);
        customer.setTotalPoints(customer.getTotalPoints() + Math.max(0, request.getPointsChange()));
        if (request.getPointsChange() > 0) {
            customer.setLifetimePoints(customer.getLifetimePoints() + request.getPointsChange());
        }

        // Update customer tier based on total points
        updateCustomerTier(customer);
        customerRepository.save(customer);

        log.info("Loyalty transaction created successfully with ID: {}", saved.getId());
        return adminMapper.toLoyaltyTransactionResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoyaltyTransactionResponse getTransactionById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        LoyaltyTransaction transaction = loyaltyTransactionRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        return adminMapper.toLoyaltyTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTransactionResponse> getTransactionsByCustomer(UUID customerId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(customerId, "customerId");
        Page<LoyaltyTransaction> page = loyaltyTransactionRepository
                .findByTenantIdAndCustomerIdAndDeletedAtIsNull(tenantId, customerId, pageable);
        return PagedResponse.of(page.map(adminMapper::toLoyaltyTransactionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTransactionResponse> getAllTransactions(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<LoyaltyTransaction> page = loyaltyTransactionRepository.findByTenantIdAndDeletedAtIsNull(tenantId,
                pageable);
        return PagedResponse.of(page.map(adminMapper::toLoyaltyTransactionResponse));
    }

    // ========== Helper Methods ==========

    /**
     * Update customer's loyalty tier based on their total points.
     */
    private void updateCustomerTier(Customer customer) {
        String tenantId = customer.getTenantId();
        Integer totalPoints = customer.getTotalPoints();

        loyaltyTierRepository.findTierForPoints(tenantId, totalPoints)
                .ifPresent(tier -> customer.setLoyaltyTierId(tier.getId()));
    }

    /**
     * Calculate points to earn from a purchase amount.
     */
    @Transactional(readOnly = true)
    public int calculatePointsForPurchase(UUID customerId, java.math.BigDecimal purchaseAmount,
            java.math.BigDecimal loyaltyPointsPerCurrency) {
        String tenantId = TenantContext.getTenantId();

        Customer customer = customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_MSG + customerId));

        java.math.BigDecimal multiplier = java.math.BigDecimal.ONE;
        if (customer.getLoyaltyTierId() != null) {
            LoyaltyTier tier = loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(
                    customer.getLoyaltyTierId(), tenantId).orElse(null);
            if (tier != null && tier.getPointsMultiplier() != null) {
                multiplier = tier.getPointsMultiplier();
            }
        }

        return purchaseAmount
                .multiply(loyaltyPointsPerCurrency)
                .multiply(multiplier)
                .intValue();
    }
}
