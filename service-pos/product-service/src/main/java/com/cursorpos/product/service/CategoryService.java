package com.cursorpos.product.service;

import com.cursorpos.product.dto.CategoryRequest;
import com.cursorpos.product.dto.CategoryResponse;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.CategoryRepository;
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
 * Service for managing product categories.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND_MSG = "Category not found with ID: ";
    private static final String ENTITY_NAME = "category";

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Creating category with code: {} for tenant: {}", request.getCode(), tenantId);

        if (categoryRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Category with code " + request.getCode() + " already exists");
        }

        Category category = productMapper.toCategory(request);
        category.setTenantId(tenantId);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getParentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
            category.setParent(parent);
        }

        Objects.requireNonNull(category, ENTITY_NAME);
        Category saved = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", saved.getId());
        return productMapper.toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        Category category = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id));
        return productMapper.toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        Category category = categoryRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with code: " + code));
        return productMapper.toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Category> page = categoryRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(productMapper::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        Objects.requireNonNull(parentId, "parentId");
        String tenantId = TenantContext.getTenantId();
        List<Category> categories = categoryRepository.findByTenantIdAndParentIdAndDeletedAtIsNull(tenantId, parentId);
        return categories.stream()
                .map(productMapper::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        String tenantId = TenantContext.getTenantId();
        List<Category> categories = categoryRepository.findByTenantIdAndIsActiveAndDeletedAtIsNull(tenantId, true);
        return categories.stream()
                .map(productMapper::toCategoryResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(request, "request");
        String tenantId = TenantContext.getTenantId();
        log.info("Updating category with ID: {} for tenant: {}", id, tenantId);

        Category category = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id));

        productMapper.updateCategoryFromRequest(request, category);

        if (request.getParentId() != null && !request.getParentId().equals(category.getParent()  != null ? category.getParent().getId() : null)) {
            Category parent = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getParentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
            category.setParent(parent);
        }

        Objects.requireNonNull(category, ENTITY_NAME);
        Category updated = categoryRepository.save(category);

        log.info("Category updated successfully with ID: {}", updated.getId());
        return productMapper.toCategoryResponse(updated);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Objects.requireNonNull(id, "id");
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting category with ID: {} for tenant: {}", id, tenantId);

        Category category = categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND_MSG + id));

        category.softDelete();
        Objects.requireNonNull(category, ENTITY_NAME);
        categoryRepository.save(category);

        log.info("Category soft-deleted successfully with ID: {}", id);
    }
}
