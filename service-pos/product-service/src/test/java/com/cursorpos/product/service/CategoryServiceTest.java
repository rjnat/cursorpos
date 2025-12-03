package com.cursorpos.product.service;

import com.cursorpos.product.dto.CategoryRequest;
import com.cursorpos.product.dto.CategoryResponse;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.CategoryRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService.
 * Tests business logic in isolation using mocked dependencies.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class CategoryServiceTest {

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private ProductMapper productMapper;

        @InjectMocks
        private CategoryService categoryService;

        private MockedStatic<TenantContext> tenantContextMock;
        private static final String TEST_TENANT = "tenant-test-001";
        private UUID categoryId;
        private Category category;
        private CategoryRequest categoryRequest;
        private CategoryResponse categoryResponse;

        @BeforeEach
        void setUp() {
                tenantContextMock = mockStatic(TenantContext.class);
                tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT);

                categoryId = UUID.randomUUID();

                category = new Category();
                category.setId(categoryId);
                category.setTenantId(TEST_TENANT);
                category.setCode("HOT-DRINKS");
                category.setName("Hot Drinks");
                category.setDescription("All hot beverages");
                category.setIsActive(true);
                category.setDisplayOrder(1);

                categoryRequest = CategoryRequest.builder()
                                .code("HOT-DRINKS")
                                .name("Hot Drinks")
                                .description("All hot beverages")
                                .isActive(true)
                                .displayOrder(1)
                                .build();

                categoryResponse = CategoryResponse.builder()
                                .id(categoryId)
                                .tenantId(TEST_TENANT)
                                .code("HOT-DRINKS")
                                .name("Hot Drinks")
                                .description("All hot beverages")
                                .isActive(true)
                                .displayOrder(1)
                                .build();
        }

        @AfterEach
        void tearDown() {
                tenantContextMock.close();
        }

        @Test
        @DisplayName("Should create category successfully")
        void testCreateCategory() {
                // Given
                when(categoryRepository.existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS")).thenReturn(false);
                when(productMapper.toCategory(categoryRequest)).thenReturn(category);
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                CategoryResponse result = categoryService.createCategory(categoryRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("HOT-DRINKS");
                assertThat(result.getName()).isEqualTo("Hot Drinks");

                verify(categoryRepository).existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS");
                verify(categoryRepository).save(any(Category.class));
                verify(productMapper).toCategory(categoryRequest);
                verify(productMapper).toCategoryResponse(category);
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate category code")
        void testCreateCategoryDuplicateCode() {
                // Given
                when(categoryRepository.existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS")).thenReturn(true);

                // When/Then
                assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("already exists");

                verify(categoryRepository).existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS");
                verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create category with parent successfully")
        void testCreateCategoryWithParent() {
                // Given
                UUID parentId = UUID.randomUUID();
                Category parent = new Category();
                parent.setId(parentId);
                parent.setTenantId(TEST_TENANT);

                categoryRequest.setParentId(parentId);

                when(categoryRepository.existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS")).thenReturn(false);
                when(productMapper.toCategory(categoryRequest)).thenReturn(category);
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(parentId, TEST_TENANT))
                                .thenReturn(Optional.of(parent));
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                CategoryResponse result = categoryService.createCategory(categoryRequest);

                // Then
                assertThat(result).isNotNull();
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(parentId, TEST_TENANT);
        }

        @Test
        @DisplayName("Should throw exception when parent category not found")
        void testCreateCategoryParentNotFound() {
                // Given
                UUID parentId = UUID.randomUUID();
                categoryRequest.setParentId(parentId);

                when(categoryRepository.existsByTenantIdAndCode(TEST_TENANT, "HOT-DRINKS")).thenReturn(false);
                when(productMapper.toCategory(categoryRequest)).thenReturn(category);
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(parentId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Parent category not found");
        }

        @Test
        @DisplayName("Should get category by ID successfully")
        void testGetCategoryById() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                CategoryResponse result = categoryService.getCategoryById(categoryId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(categoryId);
                assertThat(result.getCode()).isEqualTo("HOT-DRINKS");

                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(productMapper).toCategoryResponse(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found by ID")
        void testGetCategoryByIdNotFound() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should get category by code successfully")
        void testGetCategoryByCode() {
                // Given
                when(categoryRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "HOT-DRINKS"))
                                .thenReturn(Optional.of(category));
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                CategoryResponse result = categoryService.getCategoryByCode("HOT-DRINKS");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("HOT-DRINKS");

                verify(categoryRepository).findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "HOT-DRINKS");
        }

        @Test
        @DisplayName("Should get all categories with pagination")
        void testGetAllCategories() {
                // Given
                Category category2 = new Category();
                category2.setId(UUID.randomUUID());
                category2.setCode("COLD-DRINKS");
                category2.setName("Cold Drinks");

                List<Category> categories = Arrays.asList(category, category2);
                Page<Category> page = new PageImpl<>(categories, PageRequest.of(0, 10), categories.size());

                CategoryResponse response2 = CategoryResponse.builder()
                                .id(category2.getId())
                                .code("COLD-DRINKS")
                                .name("Cold Drinks")
                                .build();

                when(categoryRepository.findByTenantIdAndDeletedAtIsNull(eq(TEST_TENANT), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);
                when(productMapper.toCategoryResponse(category2)).thenReturn(response2);

                // When
                PagedResponse<CategoryResponse> result = categoryService.getAllCategories(PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);

                verify(categoryRepository).findByTenantIdAndDeletedAtIsNull(eq(TEST_TENANT), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get subcategories successfully")
        void testGetSubcategories() {
                // Given
                UUID parentId = UUID.randomUUID();
                Category child1 = new Category();
                child1.setId(UUID.randomUUID());
                child1.setCode("TEA");
                child1.setName("Tea");

                Category child2 = new Category();
                child2.setId(UUID.randomUUID());
                child2.setCode("COFFEE");
                child2.setName("Coffee");

                List<Category> children = Arrays.asList(child1, child2);

                when(categoryRepository.findByTenantIdAndParentIdAndDeletedAtIsNull(TEST_TENANT, parentId))
                                .thenReturn(children);
                when(productMapper.toCategoryResponse(any(Category.class)))
                                .thenReturn(CategoryResponse.builder().build());

                // When
                List<CategoryResponse> result = categoryService.getSubcategories(parentId);

                // Then
                assertThat(result).hasSize(2);
                verify(categoryRepository).findByTenantIdAndParentIdAndDeletedAtIsNull(TEST_TENANT, parentId);
        }

        @Test
        @DisplayName("Should get active categories successfully")
        void testGetActiveCategories() {
                // Given
                List<Category> activeCategories = Arrays.asList(category);

                when(categoryRepository.findByTenantIdAndIsActiveAndDeletedAtIsNull(TEST_TENANT, true))
                                .thenReturn(activeCategories);
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                List<CategoryResponse> result = categoryService.getActiveCategories();

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getIsActive()).isTrue();

                verify(categoryRepository).findByTenantIdAndIsActiveAndDeletedAtIsNull(TEST_TENANT, true);
        }

        @Test
        @DisplayName("Should update category successfully")
        void testUpdateCategory() {
                // Given
                CategoryRequest updateRequest = CategoryRequest.builder()
                                .code("HOT-DRINKS")
                                .name("Updated Name")
                                .description("Updated description")
                                .isActive(true)
                                .displayOrder(2)
                                .build();

                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                doNothing().when(productMapper).updateCategoryFromRequest(updateRequest, category);
                when(categoryRepository.save(category)).thenReturn(category);
                when(productMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

                // When
                CategoryResponse result = categoryService.updateCategory(categoryId, updateRequest);

                // Then
                assertThat(result).isNotNull();

                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(productMapper).updateCategoryFromRequest(updateRequest, category);
                verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent category")
        void testUpdateCategoryNotFound() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> categoryService.updateCategory(categoryId, categoryRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should delete category successfully (soft delete)")
        void testDeleteCategory() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.save(category)).thenReturn(category);

                // When
                categoryService.deleteCategory(categoryId);

                // Then
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void testDeleteCategoryNotFound() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void testCreateCategoryNullRequest() {
                // When/Then
                assertThatThrownBy(() -> categoryService.createCategory(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when ID is null")
        void testGetCategoryByIdNull() {
                // When/Then
                assertThatThrownBy(() -> categoryService.getCategoryById(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should update category parent when parentId is provided and different")
        void testUpdateCategoryWithParentChange() {
                // Given
                UUID newParentId = UUID.randomUUID();
                Category newParent = new Category();
                newParent.setId(newParentId);
                newParent.setCode("NEW-PARENT");

                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newParentId, TEST_TENANT))
                                .thenReturn(Optional.of(newParent));
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

                CategoryRequest request = CategoryRequest.builder()
                                .code("TEST")
                                .name("Test")
                                .parentId(newParentId)
                                .build();

                // When
                categoryService.updateCategory(categoryId, request);

                // Then
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(newParentId, TEST_TENANT);
                verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should handle updating category that has no parent")
        void testUpdateCategoryWithNullParent() {
                // Given
                category.setParent(null);
                UUID newParentId = UUID.randomUUID();
                Category newParent = new Category();
                newParent.setId(newParentId);

                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newParentId, TEST_TENANT))
                                .thenReturn(Optional.of(newParent));
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

                CategoryRequest request = CategoryRequest.builder()
                                .code("TEST")
                                .name("Test")
                                .parentId(newParentId)
                                .build();

                // When
                categoryService.updateCategory(categoryId, request);

                // Then
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(newParentId, TEST_TENANT);
                verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should throw exception when updating parent to non-existent category")
        void testUpdateCategoryParentNotFound() {
                // Given
                UUID newParentId = UUID.randomUUID();

                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newParentId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                CategoryRequest request = CategoryRequest.builder()
                                .code("TEST")
                                .name("Test")
                                .parentId(newParentId)
                                .build();

                // When/Then
                assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Parent category not found");
        }

        @Test
        @DisplayName("Should not update parent when parentId is null")
        void testUpdateCategoryWithNullParentId() {
                // Given
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

                CategoryRequest request = CategoryRequest.builder()
                                .code("TEST")
                                .name("Test")
                                .parentId(null)
                                .build();

                // When
                categoryService.updateCategory(categoryId, request);

                // Then
                verify(categoryRepository, times(1)).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should not update parent when parentId equals existing parent")
        void testUpdateCategoryWithSameParentId() {
                // Given
                UUID parentId = UUID.randomUUID();
                Category parent = new Category();
                parent.setId(parentId);
                category.setParent(parent);

                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(categoryRepository.save(any(Category.class))).thenReturn(category);
                when(productMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

                CategoryRequest request = CategoryRequest.builder()
                                .code("TEST")
                                .name("Test")
                                .parentId(parentId)
                                .build();

                // When
                categoryService.updateCategory(categoryId, request);

                // Then
                verify(categoryRepository, times(1)).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(categoryRepository).save(category);
        }
}
