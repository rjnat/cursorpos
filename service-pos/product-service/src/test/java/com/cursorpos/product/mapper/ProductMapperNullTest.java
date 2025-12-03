package com.cursorpos.product.mapper;

import com.cursorpos.product.dto.*;
import com.cursorpos.product.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ProductMapper null handling and branch coverage.
 * Covers MapStruct generated null checks.
 */
class ProductMapperNullTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapperImpl();
    }

    // ========== Category Mapping Tests ==========

    @Test
    void testToCategory_NullRequest() {
        Category result = mapper.toCategory(null);
        assertThat(result).isNull();
    }

    @Test
    void testToCategoryResponse_NullCategory() {
        CategoryResponse result = mapper.toCategoryResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToCategoryResponse_WithNullParent() {
        Category category = Category.builder()
                .code("CAT-001")
                .name("Test Category")
                .parent(null)
                .build();
        category.setId(UUID.randomUUID());

        CategoryResponse result = mapper.toCategoryResponse(category);

        assertThat(result).isNotNull();
        assertThat(result.getParentId()).isNull();
        assertThat(result.getParentName()).isNull();
    }

    @Test
    void testToCategoryResponse_WithParentButNullId() {
        Category parent = Category.builder()
                .name("Parent Category")
                .build();
        parent.setId(null);

        Category category = Category.builder()
                .code("CAT-001")
                .name("Test Category")
                .parent(parent)
                .build();
        category.setId(UUID.randomUUID());

        CategoryResponse result = mapper.toCategoryResponse(category);

        assertThat(result).isNotNull();
        assertThat(result.getParentId()).isNull();
    }

    @Test
    void testToCategoryResponse_WithParentButNullName() {
        Category parent = Category.builder()
                .name(null)
                .build();
        parent.setId(UUID.randomUUID());

        Category category = Category.builder()
                .code("CAT-001")
                .name("Test Category")
                .parent(parent)
                .build();
        category.setId(UUID.randomUUID());

        CategoryResponse result = mapper.toCategoryResponse(category);

        assertThat(result).isNotNull();
        assertThat(result.getParentName()).isNull();
    }

    @Test
    void testUpdateCategoryFromRequest_NullRequest() {
        Category category = Category.builder()
                .code("CAT-001")
                .name("Original Name")
                .build();

        mapper.updateCategoryFromRequest(null, category);

        assertThat(category.getName()).isEqualTo("Original Name");
    }

    @Test
    void testUpdateCategoryFromRequest_AllNullFields() {
        Category category = Category.builder()
                .code("CAT-001")
                .name("Original Name")
                .description("Original Description")
                .isActive(true)
                .displayOrder(1)
                .build();

        CategoryRequest request = CategoryRequest.builder()
                .code(null)
                .name(null)
                .description(null)
                .isActive(null)
                .displayOrder(null)
                .build();

        mapper.updateCategoryFromRequest(request, category);

        assertThat(category.getCode()).isEqualTo("CAT-001");
        assertThat(category.getName()).isEqualTo("Original Name");
        assertThat(category.getDescription()).isEqualTo("Original Description");
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.getDisplayOrder()).isEqualTo(1);
    }

    // ========== Product Mapping Tests ==========

    @Test
    void testToProduct_NullRequest() {
        Product result = mapper.toProduct(null);
        assertThat(result).isNull();
    }

    @Test
    void testToProductResponse_NullProduct() {
        ProductResponse result = mapper.toProductResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToProductResponse_WithNullCategory() {
        Product product = Product.builder()
                .code("PROD-001")
                .sku("SKU-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .category(null)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse result = mapper.toProductResponse(product);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getCategoryName()).isNull();
    }

    @Test
    void testToProductResponse_WithCategoryButNullId() {
        Category category = Category.builder()
                .name("Test Category")
                .build();
        category.setId(null);

        Product product = Product.builder()
                .code("PROD-001")
                .sku("SKU-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .category(category)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse result = mapper.toProductResponse(product);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isNull();
    }

    @Test
    void testToProductResponse_WithCategoryButNullName() {
        Category category = Category.builder()
                .name(null)
                .build();
        category.setId(UUID.randomUUID());

        Product product = Product.builder()
                .code("PROD-001")
                .sku("SKU-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .category(category)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse result = mapper.toProductResponse(product);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryName()).isNull();
    }

    @Test
    void testUpdateProductFromRequest_NullRequest() {
        Product product = Product.builder()
                .code("PROD-001")
                .name("Original Name")
                .build();

        mapper.updateProductFromRequest(null, product);

        assertThat(product.getName()).isEqualTo("Original Name");
    }

    @Test
    void testUpdateProductFromRequest_AllNullFields() {
        Product product = Product.builder()
                .code("PROD-001")
                .sku("SKU-001")
                .name("Original Name")
                .description("Original Description")
                .price(BigDecimal.valueOf(100))
                .cost(BigDecimal.valueOf(50))
                .taxRate(BigDecimal.valueOf(0.1))
                .unit("pcs")
                .barcode("123456")
                .imageUrl("http://example.com/image.jpg")
                .isActive(true)
                .isTrackable(true)
                .minStockLevel(10)
                .maxStockLevel(100)
                .build();

        ProductRequest request = ProductRequest.builder()
                .code(null)
                .sku(null)
                .name(null)
                .description(null)
                .price(null)
                .cost(null)
                .taxRate(null)
                .unit(null)
                .barcode(null)
                .imageUrl(null)
                .isActive(null)
                .isTrackable(null)
                .minStockLevel(null)
                .maxStockLevel(null)
                .build();

        mapper.updateProductFromRequest(request, product);

        assertThat(product.getCode()).isEqualTo("PROD-001");
        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Original Name");
        assertThat(product.getDescription()).isEqualTo("Original Description");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(product.getCost()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(product.getTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.1));
        assertThat(product.getUnit()).isEqualTo("pcs");
        assertThat(product.getBarcode()).isEqualTo("123456");
        assertThat(product.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(product.getIsActive()).isTrue();
        assertThat(product.getIsTrackable()).isTrue();
        assertThat(product.getMinStockLevel()).isEqualTo(10);
        assertThat(product.getMaxStockLevel()).isEqualTo(100);
    }

    // ========== Inventory Mapping Tests ==========

    @Test
    void testToInventory_NullRequest() {
        Inventory result = mapper.toInventory(null);
        assertThat(result).isNull();
    }

    @Test
    void testToInventoryResponse_NullInventory() {
        InventoryResponse result = mapper.toInventoryResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToInventoryResponse_WithNullProduct() {
        Inventory inventory = Inventory.builder()
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .product(null)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse result = mapper.toInventoryResponse(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isNull();
        assertThat(result.getProductCode()).isNull();
        assertThat(result.getProductName()).isNull();
    }

    @Test
    void testToInventoryResponse_WithProductButNullId() {
        Product product = Product.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();
        product.setId(null);

        Inventory inventory = Inventory.builder()
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .product(product)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse result = mapper.toInventoryResponse(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isNull();
    }

    @Test
    void testToInventoryResponse_WithProductButNullCode() {
        Product product = Product.builder()
                .code(null)
                .name("Test Product")
                .build();
        product.setId(UUID.randomUUID());

        Inventory inventory = Inventory.builder()
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .product(product)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse result = mapper.toInventoryResponse(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getProductCode()).isNull();
    }

    @Test
    void testToInventoryResponse_WithProductButNullName() {
        Product product = Product.builder()
                .code("PROD-001")
                .name(null)
                .build();
        product.setId(UUID.randomUUID());

        Inventory inventory = Inventory.builder()
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .product(product)
                .build();
        inventory.setId(UUID.randomUUID());

        InventoryResponse result = mapper.toInventoryResponse(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNull();
    }

    @Test
    void testUpdateInventoryFromRequest_NullRequest() {
        Inventory inventory = Inventory.builder()
                .quantityOnHand(100)
                .quantityReserved(10)
                .build();

        mapper.updateInventoryFromRequest(null, inventory);

        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        assertThat(inventory.getQuantityReserved()).isEqualTo(10);
    }

    @Test
    void testUpdateInventoryFromRequest_AllNullFields() {
        Inventory inventory = Inventory.builder()
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .build();

        InventoryRequest request = InventoryRequest.builder()
                .quantityOnHand(null)
                .quantityReserved(null)
                .reorderPoint(null)
                .reorderQuantity(null)
                .build();

        mapper.updateInventoryFromRequest(request, inventory);

        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        assertThat(inventory.getQuantityReserved()).isEqualTo(10);
        assertThat(inventory.getReorderPoint()).isEqualTo(20);
        assertThat(inventory.getReorderQuantity()).isEqualTo(50);
    }

    // ========== PriceHistory Mapping Tests ==========

    @Test
    void testToPriceHistoryResponse_NullPriceHistory() {
        PriceHistoryResponse result = mapper.toPriceHistoryResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToPriceHistoryResponse_WithNullProduct() {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setOldPrice(BigDecimal.valueOf(100));
        priceHistory.setNewPrice(BigDecimal.valueOf(120));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setProduct(null);

        PriceHistoryResponse result = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isNull();
        assertThat(result.getProductCode()).isNull();
        assertThat(result.getProductName()).isNull();
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullId() {
        Product product = Product.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();
        product.setId(null);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setOldPrice(BigDecimal.valueOf(100));
        priceHistory.setNewPrice(BigDecimal.valueOf(120));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setProduct(product);

        PriceHistoryResponse result = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isNull();
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullCode() {
        Product product = Product.builder()
                .code(null)
                .name("Test Product")
                .build();
        product.setId(UUID.randomUUID());

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setOldPrice(BigDecimal.valueOf(100));
        priceHistory.setNewPrice(BigDecimal.valueOf(120));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setProduct(product);

        PriceHistoryResponse result = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(result).isNotNull();
        assertThat(result.getProductCode()).isNull();
    }

    @Test
    void testToPriceHistoryResponse_WithProductButNullName() {
        Product product = Product.builder()
                .code("PROD-001")
                .name(null)
                .build();
        product.setId(UUID.randomUUID());

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setOldPrice(BigDecimal.valueOf(100));
        priceHistory.setNewPrice(BigDecimal.valueOf(120));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setProduct(product);

        PriceHistoryResponse result = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNull();
    }

    // ========== Individual Field Update Tests ==========

    @Test
    void testUpdateCategoryFromRequest_IndividualFields() {
        Category category = Category.builder()
                .code("OLD_CODE")
                .name("Old Name")
                .description("Old Description")
                .isActive(false)
                .displayOrder(1)
                .build();

        // Test updating only code
        CategoryRequest request1 = CategoryRequest.builder()
                .code("NEW_CODE")
                .build();
        mapper.updateCategoryFromRequest(request1, category);
        assertThat(category.getCode()).isEqualTo("NEW_CODE");

        // Test updating only name
        CategoryRequest request2 = CategoryRequest.builder()
                .name("New Name")
                .build();
        mapper.updateCategoryFromRequest(request2, category);
        assertThat(category.getName()).isEqualTo("New Name");

        // Test updating only description
        CategoryRequest request3 = CategoryRequest.builder()
                .description("New Description")
                .build();
        mapper.updateCategoryFromRequest(request3, category);
        assertThat(category.getDescription()).isEqualTo("New Description");

        // Test updating only isActive
        CategoryRequest request4 = CategoryRequest.builder()
                .isActive(true)
                .build();
        mapper.updateCategoryFromRequest(request4, category);
        assertThat(category.getIsActive()).isTrue();

        // Test updating only displayOrder
        CategoryRequest request5 = CategoryRequest.builder()
                .displayOrder(99)
                .build();
        mapper.updateCategoryFromRequest(request5, category);
        assertThat(category.getDisplayOrder()).isEqualTo(99);
    }

    @Test
    void testUpdateProductFromRequest_IndividualFields() {
        Product product = Product.builder()
                .code("OLD_CODE")
                .sku("OLD_SKU")
                .name("Old Name")
                .description("Old Description")
                .price(BigDecimal.valueOf(100))
                .cost(BigDecimal.valueOf(50))
                .taxRate(BigDecimal.valueOf(0.1))
                .unit("old_unit")
                .barcode("old_barcode")
                .imageUrl("old_url")
                .isActive(false)
                .isTrackable(false)
                .minStockLevel(5)
                .maxStockLevel(50)
                .build();

        // Test code
        mapper.updateProductFromRequest(ProductRequest.builder().code("NEW_CODE").build(), product);
        assertThat(product.getCode()).isEqualTo("NEW_CODE");

        // Test sku
        mapper.updateProductFromRequest(ProductRequest.builder().sku("NEW_SKU").build(), product);
        assertThat(product.getSku()).isEqualTo("NEW_SKU");

        // Test name
        mapper.updateProductFromRequest(ProductRequest.builder().name("New Name").build(), product);
        assertThat(product.getName()).isEqualTo("New Name");

        // Test description
        mapper.updateProductFromRequest(ProductRequest.builder().description("New Description").build(), product);
        assertThat(product.getDescription()).isEqualTo("New Description");

        // Test price
        mapper.updateProductFromRequest(ProductRequest.builder().price(BigDecimal.valueOf(200)).build(), product);
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));

        // Test cost
        mapper.updateProductFromRequest(ProductRequest.builder().cost(BigDecimal.valueOf(80)).build(), product);
        assertThat(product.getCost()).isEqualByComparingTo(BigDecimal.valueOf(80));

        // Test taxRate
        mapper.updateProductFromRequest(ProductRequest.builder().taxRate(BigDecimal.valueOf(0.15)).build(), product);
        assertThat(product.getTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.15));

        // Test unit
        mapper.updateProductFromRequest(ProductRequest.builder().unit("new_unit").build(), product);
        assertThat(product.getUnit()).isEqualTo("new_unit");

        // Test barcode
        mapper.updateProductFromRequest(ProductRequest.builder().barcode("new_barcode").build(), product);
        assertThat(product.getBarcode()).isEqualTo("new_barcode");

        // Test imageUrl
        mapper.updateProductFromRequest(ProductRequest.builder().imageUrl("new_url").build(), product);
        assertThat(product.getImageUrl()).isEqualTo("new_url");

        // Test isActive
        mapper.updateProductFromRequest(ProductRequest.builder().isActive(true).build(), product);
        assertThat(product.getIsActive()).isTrue();

        // Test isTrackable
        mapper.updateProductFromRequest(ProductRequest.builder().isTrackable(true).build(), product);
        assertThat(product.getIsTrackable()).isTrue();

        // Test minStockLevel
        mapper.updateProductFromRequest(ProductRequest.builder().minStockLevel(10).build(), product);
        assertThat(product.getMinStockLevel()).isEqualTo(10);

        // Test maxStockLevel
        mapper.updateProductFromRequest(ProductRequest.builder().maxStockLevel(200).build(), product);
        assertThat(product.getMaxStockLevel()).isEqualTo(200);
    }

    @Test
    void testUpdateInventoryFromRequest_IndividualFields() {
        Inventory inventory = Inventory.builder()
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .build();

        // Test quantityOnHand
        mapper.updateInventoryFromRequest(InventoryRequest.builder().quantityOnHand(200).build(), inventory);
        assertThat(inventory.getQuantityOnHand()).isEqualTo(200);

        // Test quantityReserved
        mapper.updateInventoryFromRequest(InventoryRequest.builder().quantityReserved(25).build(), inventory);
        assertThat(inventory.getQuantityReserved()).isEqualTo(25);

        // Test reorderPoint
        mapper.updateInventoryFromRequest(InventoryRequest.builder().reorderPoint(30).build(), inventory);
        assertThat(inventory.getReorderPoint()).isEqualTo(30);

        // Test reorderQuantity
        mapper.updateInventoryFromRequest(InventoryRequest.builder().reorderQuantity(100).build(), inventory);
        assertThat(inventory.getReorderQuantity()).isEqualTo(100);
    }

    @Test
    void testToCategoryResponse_WithFullyPopulatedParent() {
        UUID parentId = UUID.randomUUID();
        Category parent = Category.builder()
                .code("PARENT")
                .name("Parent Category")
                .build();
        parent.setId(parentId);

        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .code("CHILD")
                .name("Child Category")
                .description("Description")
                .isActive(true)
                .displayOrder(1)
                .parent(parent)
                .build();
        category.setId(categoryId);

        CategoryResponse result = mapper.toCategoryResponse(category);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getParentId()).isEqualTo(parentId);
        assertThat(result.getParentName()).isEqualTo("Parent Category");
    }

    @Test
    void testToProductResponse_WithFullyPopulatedCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .code("CATEGORY")
                .name("Test Category")
                .build();
        category.setId(categoryId);

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .code("PROD-001")
                .sku("SKU-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .category(category)
                .build();
        product.setId(productId);

        ProductResponse result = mapper.toProductResponse(product);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        assertThat(result.getCategoryName()).isEqualTo("Test Category");
    }

    @Test
    void testToInventoryResponse_WithFullyPopulatedProduct() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();
        product.setId(productId);

        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .product(product)
                .build();
        inventory.setId(inventoryId);

        InventoryResponse result = mapper.toInventoryResponse(inventory);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(inventoryId);
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductCode()).isEqualTo("PROD-001");
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }

    @Test
    void testToPriceHistoryResponse_WithFullyPopulatedProduct() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();
        product.setId(productId);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setOldPrice(BigDecimal.valueOf(100));
        priceHistory.setNewPrice(BigDecimal.valueOf(120));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setProduct(product);

        PriceHistoryResponse result = mapper.toPriceHistoryResponse(priceHistory);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductCode()).isEqualTo("PROD-001");
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }
}
