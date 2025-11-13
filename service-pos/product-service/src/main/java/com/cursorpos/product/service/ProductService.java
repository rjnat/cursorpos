package com.cursorpos.product.service;

import com.cursorpos.product.dto.ProductRequest;
import com.cursorpos.product.dto.ProductResponse;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.CategoryRepository;
import com.cursorpos.product.repository.PriceHistoryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing products.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found with ID: ";
    private static final String ENTITY_NAME = "product";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating product with code: {} for tenant: {}", request.getCode(), tenantId);

        if (productRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Product with code " + request.getCode() + " already exists");
        }

        if (productRepository.existsByTenantIdAndSku(tenantId, request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toProduct(request);
        product.setTenantId(tenantId);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getCategoryId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        Objects.requireNonNull(product, ENTITY_NAME);
        Product saved = productRepository.save(product);

        // Record initial price in price history
        recordPriceChange(saved, null, saved.getPrice(), "Initial price", "SYSTEM");

        log.info("Product created successfully with ID: {}", saved.getId());
        return productMapper.toProductResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MSG + id));
        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + code));
        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByTenantIdAndSkuAndDeletedAtIsNull(tenantId, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByTenantIdAndBarcodeAndDeletedAtIsNull(tenantId, barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Product> page = productRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(productMapper::toProductResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        Objects.requireNonNull(categoryId, "categoryId");
        String tenantId = TenantContext.getTenantId();
        Page<Product> page = productRepository.findByTenantIdAndCategoryIdAndDeletedAtIsNull(tenantId, categoryId, pageable);
        return PagedResponse.of(page.map(productMapper::toProductResponse));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        String tenantId = TenantContext.getTenantId();
        List<Product> products = productRepository.findByTenantIdAndIsActiveAndDeletedAtIsNull(tenantId, true);
        return products.stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(String search, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Product> page = productRepository.searchProducts(tenantId, search, pageable);
        return PagedResponse.of(page.map(productMapper::toProductResponse));
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Updating product with ID: {} for tenant: {}", id, tenantId);

        Product product = productRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MSG + id));

        BigDecimal oldPrice = product.getPrice();

        productMapper.updateProductFromRequest(request, product);

        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory() != null ? product.getCategory().getId() : null)) {
            Category category = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getCategoryId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        Objects.requireNonNull(product, ENTITY_NAME);
        Product updated = productRepository.save(product);

        // Record price change if price was updated
        if (request.getPrice() != null && oldPrice.compareTo(request.getPrice()) != 0) {
            recordPriceChange(updated, oldPrice, request.getPrice(), "Price update", "SYSTEM");
        }

        log.info("Product updated successfully with ID: {}", updated.getId());
        return productMapper.toProductResponse(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting product with ID: {} for tenant: {}", id, tenantId);

        Product product = productRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_MSG + id));

        product.softDelete();
        Objects.requireNonNull(product, ENTITY_NAME);
        productRepository.save(product);

        log.info("Product soft-deleted successfully with ID: {}", id);
    }

    private void recordPriceChange(Product product, BigDecimal oldPrice, BigDecimal newPrice, String reason, String changedBy) {
        PriceHistory priceHistory = PriceHistory.builder()
                .tenantId(product.getTenantId())
                .product(product)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .effectiveFrom(LocalDateTime.now())
                .changedBy(changedBy)
                .reason(reason)
                .build();

        priceHistoryRepository.save(priceHistory);
    }
}
