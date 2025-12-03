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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Tests business logic in isolation using mocked dependencies.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class ProductServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private PriceHistoryRepository priceHistoryRepository;

        @Mock
        private ProductMapper productMapper;

        @InjectMocks
        private ProductService productService;

        private MockedStatic<TenantContext> tenantContextMock;
        private static final String TEST_TENANT = "tenant-test-001";
        private UUID productId;
        private UUID categoryId;
        private Product product;
        private Category category;
        private ProductRequest productRequest;
        private ProductResponse productResponse;

        @BeforeEach
        void setUp() {
                tenantContextMock = mockStatic(TenantContext.class);
                tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT);

                productId = UUID.randomUUID();
                categoryId = UUID.randomUUID();

                category = new Category();
                category.setId(categoryId);
                category.setTenantId(TEST_TENANT);
                category.setCode("HOT-DRINKS");
                category.setName("Hot Drinks");

                product = new Product();
                product.setId(productId);
                product.setTenantId(TEST_TENANT);
                product.setCode("ESPRESSO");
                product.setName("Espresso");
                product.setDescription("Double shot espresso");
                product.setSku("ESP-001");
                product.setBarcode("1234567890123");
                product.setPrice(new BigDecimal("3.50"));
                product.setCost(new BigDecimal("1.20"));
                product.setCategory(category);
                product.setIsActive(true);

                productRequest = ProductRequest.builder()
                                .code("ESPRESSO")
                                .name("Espresso")
                                .description("Double shot espresso")
                                .sku("ESP-001")
                                .barcode("1234567890123")
                                .price(new BigDecimal("3.50"))
                                .cost(new BigDecimal("1.20"))
                                .categoryId(categoryId)
                                .isActive(true)
                                .build();

                productResponse = ProductResponse.builder()
                                .id(productId)
                                .tenantId(TEST_TENANT)
                                .code("ESPRESSO")
                                .name("Espresso")
                                .description("Double shot espresso")
                                .sku("ESP-001")
                                .barcode("1234567890123")
                                .price(new BigDecimal("3.50"))
                                .cost(new BigDecimal("1.20"))
                                .isActive(true)
                                .build();
        }

        @AfterEach
        void tearDown() {
                tenantContextMock.close();
        }

        @Test
        @DisplayName("Should create product successfully")
        void testCreateProduct() {
                // Given
                when(productRepository.existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO")).thenReturn(false);
                when(productRepository.existsByTenantIdAndSku(TEST_TENANT, "ESP-001")).thenReturn(false);
                when(productMapper.toProduct(productRequest)).thenReturn(product);
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(new PriceHistory());
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.createProduct(productRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("ESPRESSO");
                assertThat(result.getSku()).isEqualTo("ESP-001");

                verify(productRepository).existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO");
                verify(productRepository).existsByTenantIdAndSku(TEST_TENANT, "ESP-001");
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT);
                verify(productRepository).save(any(Product.class));
                verify(priceHistoryRepository).save(any(PriceHistory.class));
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate product code")
        void testCreateProductDuplicateCode() {
                // Given
                when(productRepository.existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO")).thenReturn(true);

                // When/Then
                assertThatThrownBy(() -> productService.createProduct(productRequest))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("already exists");

                verify(productRepository).existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO");
                verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate SKU")
        void testCreateProductDuplicateSku() {
                // Given
                when(productRepository.existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO")).thenReturn(false);
                when(productRepository.existsByTenantIdAndSku(TEST_TENANT, "ESP-001")).thenReturn(true);

                // When/Then
                assertThatThrownBy(() -> productService.createProduct(productRequest))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("SKU");

                verify(productRepository).existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO");
                verify(productRepository).existsByTenantIdAndSku(TEST_TENANT, "ESP-001");
                verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void testCreateProductCategoryNotFound() {
                // Given
                when(productRepository.existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO")).thenReturn(false);
                when(productRepository.existsByTenantIdAndSku(TEST_TENANT, "ESP-001")).thenReturn(false);
                when(productMapper.toProduct(productRequest)).thenReturn(product);
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.createProduct(productRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should record price history when creating product")
        void testCreateProductRecordsPriceHistory() {
                // Given
                when(productRepository.existsByTenantIdAndCode(TEST_TENANT, "ESPRESSO")).thenReturn(false);
                when(productRepository.existsByTenantIdAndSku(TEST_TENANT, "ESP-001")).thenReturn(false);
                when(productMapper.toProduct(productRequest)).thenReturn(product);
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, TEST_TENANT))
                                .thenReturn(Optional.of(category));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(new PriceHistory());
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                productService.createProduct(productRequest);

                // Then
                ArgumentCaptor<PriceHistory> priceHistoryCaptor = ArgumentCaptor.forClass(PriceHistory.class);
                verify(priceHistoryRepository).save(priceHistoryCaptor.capture());

                PriceHistory captured = priceHistoryCaptor.getValue();
                assertThat(captured.getNewPrice()).isEqualTo(new BigDecimal("3.50"));
                assertThat(captured.getOldPrice()).isNull();
                assertThat(captured.getReason()).isEqualTo("Initial price");
        }

        @Test
        @DisplayName("Should get product by ID successfully")
        void testGetProductById() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.getProductById(productId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(productId);

                verify(productRepository).findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT);
        }

        @Test
        @DisplayName("Should throw exception when product not found by ID")
        void testGetProductByIdNotFound() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.getProductById(productId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should get product by code successfully")
        void testGetProductByCode() {
                // Given
                when(productRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "ESPRESSO"))
                                .thenReturn(Optional.of(product));
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.getProductByCode("ESPRESSO");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("ESPRESSO");

                verify(productRepository).findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "ESPRESSO");
        }

        @Test
        @DisplayName("Should get product by SKU successfully")
        void testGetProductBySku() {
                // Given
                when(productRepository.findByTenantIdAndSkuAndDeletedAtIsNull(TEST_TENANT, "ESP-001"))
                                .thenReturn(Optional.of(product));
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.getProductBySku("ESP-001");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getSku()).isEqualTo("ESP-001");

                verify(productRepository).findByTenantIdAndSkuAndDeletedAtIsNull(TEST_TENANT, "ESP-001");
        }

        @Test
        @DisplayName("Should get product by barcode successfully")
        void testGetProductByBarcode() {
                // Given
                when(productRepository.findByTenantIdAndBarcodeAndDeletedAtIsNull(TEST_TENANT, "1234567890123"))
                                .thenReturn(Optional.of(product));
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.getProductByBarcode("1234567890123");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getBarcode()).isEqualTo("1234567890123");

                verify(productRepository).findByTenantIdAndBarcodeAndDeletedAtIsNull(TEST_TENANT, "1234567890123");
        }

        @Test
        @DisplayName("Should get all products with pagination")
        void testGetAllProducts() {
                // Given
                Product product2 = new Product();
                product2.setId(UUID.randomUUID());
                product2.setCode("CAPPUCCINO");

                List<Product> products = Arrays.asList(product, product2);
                Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), products.size());

                when(productRepository.findByTenantIdAndDeletedAtIsNull(eq(TEST_TENANT), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toProductResponse(any(Product.class)))
                                .thenReturn(productResponse);

                // When
                PagedResponse<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);

                verify(productRepository).findByTenantIdAndDeletedAtIsNull(eq(TEST_TENANT), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get products by category")
        void testGetProductsByCategory() {
                // Given
                List<Product> products = Arrays.asList(product);
                Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), products.size());

                when(productRepository.findByTenantIdAndCategoryIdAndDeletedAtIsNull(
                                eq(TEST_TENANT), eq(categoryId), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                PagedResponse<ProductResponse> result = productService.getProductsByCategory(categoryId,
                                PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);

                verify(productRepository).findByTenantIdAndCategoryIdAndDeletedAtIsNull(
                                eq(TEST_TENANT), eq(categoryId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get active products successfully")
        void testGetActiveProducts() {
                // Given
                List<Product> activeProducts = Arrays.asList(product);

                when(productRepository.findByTenantIdAndIsActiveAndDeletedAtIsNull(TEST_TENANT, true))
                                .thenReturn(activeProducts);
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                List<ProductResponse> result = productService.getActiveProducts();

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getIsActive()).isTrue();

                verify(productRepository).findByTenantIdAndIsActiveAndDeletedAtIsNull(TEST_TENANT, true);
        }

        @Test
        @DisplayName("Should search products successfully")
        void testSearchProducts() {
                // Given
                List<Product> products = Arrays.asList(product);
                Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), products.size());

                when(productRepository.searchProducts(eq(TEST_TENANT), eq("espresso"), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                PagedResponse<ProductResponse> result = productService.searchProducts("espresso",
                                PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);

                verify(productRepository).searchProducts(eq(TEST_TENANT), eq("espresso"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should update product successfully")
        void testUpdateProduct() {
                // Given
                ProductRequest updateRequest = ProductRequest.builder()
                                .code("ESPRESSO")
                                .name("Updated Espresso")
                                .description("Triple shot espresso")
                                .sku("ESP-001")
                                .barcode("1234567890123")
                                .price(new BigDecimal("4.00"))
                                .cost(new BigDecimal("1.50"))
                                .categoryId(categoryId)
                                .isActive(true)
                                .build();

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                doNothing().when(productMapper).updateProductFromRequest(updateRequest, product);
                when(productRepository.save(product)).thenReturn(product);
                when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(new PriceHistory());
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                ProductResponse result = productService.updateProduct(productId, updateRequest);

                // Then
                assertThat(result).isNotNull();

                verify(productRepository).findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT);
                verify(productMapper).updateProductFromRequest(updateRequest, product);
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should record price history when price changes")
        void testUpdateProductRecordsPriceHistoryOnPriceChange() {
                // Given
                ProductRequest updateRequest = ProductRequest.builder()
                                .code("ESPRESSO")
                                .name("Espresso")
                                .price(new BigDecimal("4.50"))
                                .categoryId(categoryId)
                                .build();

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                doNothing().when(productMapper).updateProductFromRequest(updateRequest, product);
                when(productRepository.save(product)).thenReturn(product);
                when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(new PriceHistory());
                when(productMapper.toProductResponse(product)).thenReturn(productResponse);

                // When
                productService.updateProduct(productId, updateRequest);

                // Then
                ArgumentCaptor<PriceHistory> priceHistoryCaptor = ArgumentCaptor.forClass(PriceHistory.class);
                verify(priceHistoryRepository).save(priceHistoryCaptor.capture());

                PriceHistory captured = priceHistoryCaptor.getValue();
                assertThat(captured.getOldPrice()).isEqualTo(new BigDecimal("3.50"));
                assertThat(captured.getNewPrice()).isEqualTo(new BigDecimal("4.50"));
                assertThat(captured.getReason()).isEqualTo("Price update");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void testUpdateProductNotFound() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.updateProduct(productId, productRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should delete product successfully (soft delete)")
        void testDeleteProduct() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productRepository.save(product)).thenReturn(product);

                // When
                productService.deleteProduct(productId);

                // Then
                verify(productRepository).findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT);
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void testDeleteProductNotFound() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.deleteProduct(productId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void testCreateProductNullRequest() {
                // When/Then
                assertThatThrownBy(() -> productService.createProduct(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when ID is null")
        void testGetProductByIdNull() {
                // When/Then
                assertThatThrownBy(() -> productService.getProductById(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should update product category when categoryId is provided and different")
        void testUpdateProductWithCategoryChange() {
                // Given
                UUID newCategoryId = UUID.randomUUID();
                Category newCategory = new Category();
                newCategory.setId(newCategoryId);
                newCategory.setCode("NEW-CAT");

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newCategoryId, TEST_TENANT))
                                .thenReturn(Optional.of(newCategory));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .categoryId(newCategoryId)
                                .price(product.getPrice())
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(productRepository).findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT);
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(newCategoryId, TEST_TENANT);
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should handle updating product that has no category")
        void testUpdateProductWithNullCategory() {
                // Given
                product.setCategory(null);
                UUID newCategoryId = UUID.randomUUID();
                Category newCategory = new Category();
                newCategory.setId(newCategoryId);

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newCategoryId, TEST_TENANT))
                                .thenReturn(Optional.of(newCategory));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .categoryId(newCategoryId)
                                .price(product.getPrice())
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(categoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(newCategoryId, TEST_TENANT);
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should not record price history when price doesn't change")
        void testUpdateProductWithSamePrice() {
                // Given
                BigDecimal originalPrice = new BigDecimal("99.99");
                product.setPrice(originalPrice);

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .price(originalPrice)
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(productRepository).save(product);
                verify(priceHistoryRepository, never()).save(any(PriceHistory.class));
        }

        @Test
        @DisplayName("Should handle update when request price is null")
        void testUpdateProductWithNullPrice() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .price(null)
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(productRepository).save(product);
                verify(priceHistoryRepository, never()).save(any(PriceHistory.class));
        }

        @Test
        @DisplayName("Should throw exception when updating category to non-existent category")
        void testUpdateProductCategoryNotFound() {
                // Given
                UUID newCategoryId = UUID.randomUUID();

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(newCategoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .categoryId(newCategoryId)
                                .build();

                // When/Then
                assertThatThrownBy(() -> productService.updateProduct(productId, request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should not update category when categoryId is null")
        void testUpdateProductWithNullCategoryId() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .categoryId(null)
                                .price(product.getPrice())
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(categoryRepository, never()).findByIdAndTenantIdAndDeletedAtIsNull(any(), any());
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should not update category when categoryId equals existing category")
        void testUpdateProductWithSameCategoryId() {
                // Given
                product.setCategory(category);

                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

                ProductRequest request = ProductRequest.builder()
                                .code("TEST")
                                .sku("TEST-SKU")
                                .name("Test")
                                .categoryId(categoryId)
                                .price(product.getPrice())
                                .build();

                // When
                productService.updateProduct(productId, request);

                // Then
                verify(categoryRepository, never()).findByIdAndTenantIdAndDeletedAtIsNull(any(), any());
                verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw exception when getting product by code not found")
        void testGetProductByCode_NotFound() {
                // Given
                when(productRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND"))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.getProductByCode("NOTFOUND"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found with code: NOTFOUND");

                verify(productRepository).findByTenantIdAndCodeAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND");
        }

        @Test
        @DisplayName("Should throw exception when getting product by SKU not found")
        void testGetProductBySku_NotFound() {
                // Given
                when(productRepository.findByTenantIdAndSkuAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND"))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.getProductBySku("NOTFOUND"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found with SKU: NOTFOUND");

                verify(productRepository).findByTenantIdAndSkuAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND");
        }

        @Test
        @DisplayName("Should throw exception when getting product by barcode not found")
        void testGetProductByBarcode_NotFound() {
                // Given
                when(productRepository.findByTenantIdAndBarcodeAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND"))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> productService.getProductByBarcode("NOTFOUND"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found with barcode: NOTFOUND");

                verify(productRepository).findByTenantIdAndBarcodeAndDeletedAtIsNull(TEST_TENANT, "NOTFOUND");
        }
}
