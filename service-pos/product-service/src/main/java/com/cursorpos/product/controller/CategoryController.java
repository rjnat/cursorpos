package com.cursorpos.product.controller;

import com.cursorpos.product.dto.CategoryRequest;
import com.cursorpos.product.dto.CategoryResponse;
import com.cursorpos.product.service.CategoryService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for category management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryByCode(@PathVariable String code) {
        CategoryResponse response = categoryService.getCategoryByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> getAllCategories(Pageable pageable) {
        PagedResponse<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(@PathVariable UUID parentId) {
        List<CategoryResponse> response = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        List<CategoryResponse> response = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
