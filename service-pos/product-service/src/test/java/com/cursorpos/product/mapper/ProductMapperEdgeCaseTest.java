package com.cursorpos.product.mapper;

import com.cursorpos.product.entity.*;
import com.cursorpos.product.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Edge case tests to achieve 100% coverage for mapper helper methods.
 * Tests the specific scenario where nested entities exist but their ID/name
 * fields are null.
 */
@ExtendWith(MockitoExtension.class)
class ProductMapperEdgeCaseTest {

    @InjectMocks
    private ProductMapperImpl mapper = new ProductMapperImpl();

    @Test
    void testToCategoryResponse_WithParentButNullId() {
        // Scenario: Category has a parent entity, but parent.getId() returns null
        Category parent = Category.builder()
                .name("ParentName")
                .description("Parent Description")
                .build();
        // Explicitly set ID to null to trigger the "id == null" branch
        parent.setId(null);

        Category category = Category.builder()
                .code("CAT001")
                .name("Test Category")
                .description("Test Description")
                .parent(parent)
                .build();
        category.setId(UUID.randomUUID());

        CategoryResponse response = mapper.toCategoryResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("CAT001");
        assertThat(response.getParentId()).isNull(); // Should be null since parent.getId() is null
    }

    @Test
    void testToCategoryResponse_WithParentButNullName() {
        // Scenario: Category has a parent entity with ID, but parent.getName() returns
        // null
        Category parent = Category.builder()
                .description("Parent Description")
                .build();
        parent.setId(UUID.randomUUID());
        // Name is already null from builder

        Category category = Category.builder()
                .code("CAT002")
                .name("Test Category")
                .description("Test Description")
                .parent(parent)
                .build();
        category.setId(UUID.randomUUID());

        CategoryResponse response = mapper.toCategoryResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("CAT002");
        assertThat(response.getParentName()).isNull(); // Should be null since parent.getName() is null
    }

    @Test
    void testToProductResponse_WithCategoryButNullId() {
        // Scenario: Product has a category entity, but category.getId() returns null
        Category category = Category.builder()
                .code("CAT003")
                .name("Category Name")
                .build();
        category.setId(null);

        Product product = Product.builder()
                .code("PROD001")
                .name("Test Product")
                .description("Test Description")
                .sku("SKU001")
                .barcode("BAR001")
                .unit("PCS")
                .category(category)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse response = mapper.toProductResponse(product);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("PROD001");
        assertThat(response.getCategoryId()).isNull(); // Should be null since category.getId() is null
    }

    @Test
    void testToProductResponse_WithCategoryButNullName() {
        // Scenario: Product has a category entity with ID, but category.getName()
        // returns null
        Category category = Category.builder()
                .code("CAT004")
                .description("Category Description")
                .build();
        category.setId(UUID.randomUUID());
        // Name is already null from builder

        Product product = Product.builder()
                .code("PROD002")
                .name("Test Product")
                .description("Test Description")
                .sku("SKU002")
                .barcode("BAR002")
                .unit("PCS")
                .category(category)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse response = mapper.toProductResponse(product);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("PROD002");
        assertThat(response.getCategoryName()).isNull(); // Should be null since category.getName() is null
    }

    @Test
    void testToInventoryResponse_WithProductButNullId() {
        // Scenario: Inventory has a product entity, but product.getId() returns null
        Product product = Product.builder()
                .code("PROD003")
                .name("Product Name")
                .build();
        product.setId(null);

        Inventory inventory = Inventory.builder()
                .product(product)
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .reorderPoint(20)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse response = mapper.toInventoryResponse(inventory);

        assertThat(response).isNotNull();
        assertThat(response.getQuantityOnHand()).isEqualTo(100);
        assertThat(response.getProductId()).isNull(); // Should be null since product.getId() is null
    }

    @Test
    void testToInventoryResponse_WithProductButNullCode() {
        // Scenario: Inventory has a product entity with ID, but product.getCode()
        // returns null
        Product product = Product.builder()
                .name("Product Name")
                .build();
        product.setId(UUID.randomUUID());
        // Code is already null from builder

        Inventory inventory = Inventory.builder()
                .product(product)
                .branchId(UUID.randomUUID())
                .quantityOnHand(150)
                .reorderPoint(25)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse response = mapper.toInventoryResponse(inventory);

        assertThat(response).isNotNull();
        assertThat(response.getQuantityOnHand()).isEqualTo(150);
        assertThat(response.getProductCode()).isNull(); // Should be null since product.getCode() is null
    }

    @Test
    void testToInventoryResponse_WithProductButNullName() {
        // Scenario: Inventory has a product entity with ID and code, but
        // product.getName() returns null
        Product product = Product.builder()
                .code("PROD004")
                .build();
        product.setId(UUID.randomUUID());
        // Name is already null from builder

        Inventory inventory = Inventory.builder()
                .product(product)
                .branchId(UUID.randomUUID())
                .quantityOnHand(200)
                .reorderPoint(30)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse response = mapper.toInventoryResponse(inventory);

        assertThat(response).isNotNull();
        assertThat(response.getQuantityOnHand()).isEqualTo(200);
        assertThat(response.getProductName()).isNull(); // Should be null since product.getName() is null
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullId() {
        // Scenario: PriceHistory has a product entity, but product.getId() returns null
        Product product = Product.builder()
                .code("PROD005")
                .name("Product Name")
                .build();
        product.setId(null);

        PriceHistory priceHistory = PriceHistory.builder()
                .product(product)
                .newPrice(new BigDecimal("99.99"))
                .effectiveFrom(LocalDateTime.now())
                .build();
        priceHistory.setId(UUID.randomUUID());

        PriceHistoryResponse response = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(response).isNotNull();
        assertThat(response.getNewPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.getProductId()).isNull(); // Should be null since product.getId() is null
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullCode() {
        // Scenario: PriceHistory has a product entity with ID, but product.getCode()
        // returns null
        Product product = Product.builder()
                .name("Product Name")
                .build();
        product.setId(UUID.randomUUID());
        // Code is already null from builder

        PriceHistory priceHistory = PriceHistory.builder()
                .product(product)
                .newPrice(new BigDecimal("149.99"))
                .effectiveFrom(LocalDateTime.now())
                .build();
        priceHistory.setId(UUID.randomUUID());

        PriceHistoryResponse response = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(response).isNotNull();
        assertThat(response.getNewPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(response.getProductCode()).isNull(); // Should be null since product.getCode() is null
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullName() {
        // Scenario: PriceHistory has a product entity with ID and code, but
        // product.getName() returns null
        Product product = Product.builder()
                .code("PROD006")
                .build();
        product.setId(UUID.randomUUID());
        // Name is already null from builder

        PriceHistory priceHistory = PriceHistory.builder()
                .product(product)
                .newPrice(new BigDecimal("199.99"))
                .effectiveFrom(LocalDateTime.now())
                .build();
        priceHistory.setId(UUID.randomUUID());

        PriceHistoryResponse response = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(response).isNotNull();
        assertThat(response.getNewPrice()).isEqualByComparingTo(new BigDecimal("199.99"));
        assertThat(response.getProductName()).isNull(); // Should be null since product.getName() is null
    }

    @Test
    void testIsLowStock_WithNullReorderPoint() {
        // Scenario: Inventory has null reorderPoint, isLowStock returns false
        Inventory inventory = Inventory.builder()
                .quantityOnHand(5)
                .reorderPoint(null) // Null reorder point
                .build();

        boolean result = mapper.isLowStock(inventory);

        assertThat(result).isFalse(); // No reorder point, so not low stock
    }

    @Test
    void testIsLowStock_WithReorderPoint() {
        // Scenario: Inventory has reorderPoint and quantityOnHand below it
        Inventory inventory = Inventory.builder()
                .quantityOnHand(5)
                .reorderPoint(10) // Has reorder point
                .build();

        boolean result = mapper.isLowStock(inventory);

        assertThat(result).isTrue(); // quantity (5) <= reorderPoint (10)
    }
}
