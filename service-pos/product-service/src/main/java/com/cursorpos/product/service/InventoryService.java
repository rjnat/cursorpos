package com.cursorpos.product.service;

import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.InventoryResponse;
import com.cursorpos.product.dto.StockAdjustmentRequest;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.InventoryRepository;
import com.cursorpos.product.repository.ProductRepository;
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
 * Service for managing product inventory.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final String INVENTORY_NOT_FOUND_MSG = "Inventory not found with ID: ";
    private static final String ENTITY_NAME = "inventory";

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public InventoryResponse createOrUpdateInventory(InventoryRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating/updating inventory for product: {} at branch: {} for tenant: {}", 
                 request.getProductId(), request.getBranchId(), tenantId);

        Product product = productRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getProductId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                tenantId, request.getProductId(), request.getBranchId())
                .orElse(null);

        if (inventory == null) {
            inventory = productMapper.toInventory(request);
            inventory.setTenantId(tenantId);
            inventory.setProduct(product);
        } else {
            productMapper.updateInventoryFromRequest(request, inventory);
        }

        Objects.requireNonNull(inventory, ENTITY_NAME);
        Inventory saved = inventoryRepository.save(inventory);

        log.info("Inventory created/updated successfully with ID: {}", saved.getId());
        return productMapper.toInventoryResponse(saved);
    }

    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Adjusting stock for product: {} at branch: {} - type: {}, quantity: {}", 
                 request.getProductId(), request.getBranchId(), request.getType(), request.getQuantity());

        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                tenantId, request.getProductId(), request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Inventory not found for product: " + request.getProductId() + " at branch: " + request.getBranchId()));

        int newQuantity = switch (request.getType()) {
            case ADD -> inventory.getQuantityOnHand() + request.getQuantity();
            case SUBTRACT -> {
                int result = inventory.getQuantityOnHand() - request.getQuantity();
                if (result < 0) {
                    throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getQuantityOnHand());
                }
                yield result;
            }
            case SET -> request.getQuantity();
        };

        inventory.setQuantityOnHand(newQuantity);
        Objects.requireNonNull(inventory, ENTITY_NAME);
        Inventory updated = inventoryRepository.save(inventory);

        log.info("Stock adjusted successfully. New quantity: {}", updated.getQuantityOnHand());
        return productMapper.toInventoryResponse(updated);
    }

    @Transactional
    public InventoryResponse reserveStock(UUID productId, UUID branchId, Integer quantity) {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(branchId, "branchId");
        Objects.requireNonNull(quantity, "quantity");
        String tenantId = TenantContext.getTenantId();
        log.info("Reserving {} units of product: {} at branch: {}", quantity, productId, branchId);

        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                tenantId, productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Inventory not found for product: " + productId + " at branch: " + branchId));

        if (inventory.getQuantityAvailable() < quantity) {
            throw new IllegalArgumentException("Insufficient stock available. Available: " + inventory.getQuantityAvailable());
        }

        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        Objects.requireNonNull(inventory, ENTITY_NAME);
        Inventory updated = inventoryRepository.save(inventory);

        log.info("Stock reserved successfully. Reserved: {}, Available: {}", 
                 updated.getQuantityReserved(), updated.getQuantityAvailable());
        return productMapper.toInventoryResponse(updated);
    }

    @Transactional
    public InventoryResponse releaseStock(UUID productId, UUID branchId, Integer quantity) {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(branchId, "branchId");
        Objects.requireNonNull(quantity, "quantity");
        String tenantId = TenantContext.getTenantId();
        log.info("Releasing {} units of product: {} at branch: {}", quantity, productId, branchId);

        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                tenantId, productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Inventory not found for product: " + productId + " at branch: " + branchId));

        int newReserved = inventory.getQuantityReserved() - quantity;
        if (newReserved < 0) {
            throw new IllegalArgumentException("Cannot release more than reserved. Reserved: " + inventory.getQuantityReserved());
        }

        inventory.setQuantityReserved(newReserved);
        Objects.requireNonNull(inventory, ENTITY_NAME);
        Inventory updated = inventoryRepository.save(inventory);

        log.info("Stock released successfully. Reserved: {}, Available: {}", 
                 updated.getQuantityReserved(), updated.getQuantityAvailable());
        return productMapper.toInventoryResponse(updated);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        Inventory inventory = inventoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_NOT_FOUND_MSG + id));
        return productMapper.toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductAndBranch(UUID productId, UUID branchId) {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(branchId, "branchId");
        String tenantId = TenantContext.getTenantId();
        Inventory inventory = inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                tenantId, productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Inventory not found for product: " + productId + " at branch: " + branchId));
        return productMapper.toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getAllInventory(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Inventory> page = inventoryRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(productMapper::toInventoryResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getInventoryByBranch(UUID branchId, Pageable pageable) {
        Objects.requireNonNull(branchId, "branchId");
        String tenantId = TenantContext.getTenantId();
        Page<Inventory> page = inventoryRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(tenantId, branchId, pageable);
        return PagedResponse.of(page.map(productMapper::toInventoryResponse));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(UUID productId) {
        Objects.requireNonNull(productId, "productId");
        String tenantId = TenantContext.getTenantId();
        List<Inventory> inventories = inventoryRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId);
        return inventories.stream()
                .map(productMapper::toInventoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems() {
        String tenantId = TenantContext.getTenantId();
        List<Inventory> inventories = inventoryRepository.findLowStockItems(tenantId);
        return inventories.stream()
                .map(productMapper::toInventoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItemsByBranch(UUID branchId) {
        Objects.requireNonNull(branchId, "branchId");
        String tenantId = TenantContext.getTenantId();
        List<Inventory> inventories = inventoryRepository.findLowStockItemsByBranch(tenantId, branchId);
        return inventories.stream()
                .map(productMapper::toInventoryResponse)
                .toList();
    }
}
