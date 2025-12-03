package com.cursorpos.product.mapper;

import com.cursorpos.product.dto.CategoryRequest;
import com.cursorpos.product.dto.CategoryResponse;
import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.InventoryResponse;
import com.cursorpos.product.dto.PriceHistoryResponse;
import com.cursorpos.product.dto.ProductRequest;
import com.cursorpos.product.dto.ProductResponse;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T23:16:56+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Category toCategory(CategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.code( request.getCode() );
        category.description( request.getDescription() );
        category.displayOrder( request.getDisplayOrder() );
        category.isActive( request.getIsActive() );
        category.name( request.getName() );

        return category.build();
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.parentId( categoryParentId( category ) );
        categoryResponse.parentName( categoryParentName( category ) );
        categoryResponse.code( category.getCode() );
        categoryResponse.createdAt( category.getCreatedAt() );
        categoryResponse.description( category.getDescription() );
        categoryResponse.displayOrder( category.getDisplayOrder() );
        categoryResponse.id( category.getId() );
        categoryResponse.isActive( category.getIsActive() );
        categoryResponse.name( category.getName() );
        categoryResponse.tenantId( category.getTenantId() );
        categoryResponse.updatedAt( category.getUpdatedAt() );

        return categoryResponse.build();
    }

    @Override
    public void updateCategoryFromRequest(CategoryRequest request, Category category) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            category.setCode( request.getCode() );
        }
        if ( request.getDescription() != null ) {
            category.setDescription( request.getDescription() );
        }
        if ( request.getDisplayOrder() != null ) {
            category.setDisplayOrder( request.getDisplayOrder() );
        }
        if ( request.getIsActive() != null ) {
            category.setIsActive( request.getIsActive() );
        }
        if ( request.getName() != null ) {
            category.setName( request.getName() );
        }
    }

    @Override
    public Product toProduct(ProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.barcode( request.getBarcode() );
        product.code( request.getCode() );
        product.cost( request.getCost() );
        product.description( request.getDescription() );
        product.imageUrl( request.getImageUrl() );
        product.isActive( request.getIsActive() );
        product.isTrackable( request.getIsTrackable() );
        product.maxStockLevel( request.getMaxStockLevel() );
        product.minStockLevel( request.getMinStockLevel() );
        product.name( request.getName() );
        product.price( request.getPrice() );
        product.sku( request.getSku() );
        product.taxRate( request.getTaxRate() );
        product.unit( request.getUnit() );

        return product.build();
    }

    @Override
    public ProductResponse toProductResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.categoryId( productCategoryId( product ) );
        productResponse.categoryName( productCategoryName( product ) );
        productResponse.barcode( product.getBarcode() );
        productResponse.code( product.getCode() );
        productResponse.cost( product.getCost() );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.description( product.getDescription() );
        productResponse.id( product.getId() );
        productResponse.imageUrl( product.getImageUrl() );
        productResponse.isActive( product.getIsActive() );
        productResponse.isTrackable( product.getIsTrackable() );
        productResponse.maxStockLevel( product.getMaxStockLevel() );
        productResponse.minStockLevel( product.getMinStockLevel() );
        productResponse.name( product.getName() );
        productResponse.price( product.getPrice() );
        productResponse.sku( product.getSku() );
        productResponse.taxRate( product.getTaxRate() );
        productResponse.tenantId( product.getTenantId() );
        productResponse.unit( product.getUnit() );
        productResponse.updatedAt( product.getUpdatedAt() );

        return productResponse.build();
    }

    @Override
    public void updateProductFromRequest(ProductRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getBarcode() != null ) {
            product.setBarcode( request.getBarcode() );
        }
        if ( request.getCode() != null ) {
            product.setCode( request.getCode() );
        }
        if ( request.getCost() != null ) {
            product.setCost( request.getCost() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getImageUrl() != null ) {
            product.setImageUrl( request.getImageUrl() );
        }
        if ( request.getIsActive() != null ) {
            product.setIsActive( request.getIsActive() );
        }
        if ( request.getIsTrackable() != null ) {
            product.setIsTrackable( request.getIsTrackable() );
        }
        if ( request.getMaxStockLevel() != null ) {
            product.setMaxStockLevel( request.getMaxStockLevel() );
        }
        if ( request.getMinStockLevel() != null ) {
            product.setMinStockLevel( request.getMinStockLevel() );
        }
        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        if ( request.getSku() != null ) {
            product.setSku( request.getSku() );
        }
        if ( request.getTaxRate() != null ) {
            product.setTaxRate( request.getTaxRate() );
        }
        if ( request.getUnit() != null ) {
            product.setUnit( request.getUnit() );
        }
    }

    @Override
    public Inventory toInventory(InventoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Inventory.InventoryBuilder inventory = Inventory.builder();

        inventory.branchId( request.getBranchId() );
        inventory.quantityOnHand( request.getQuantityOnHand() );
        inventory.quantityReserved( request.getQuantityReserved() );
        inventory.reorderPoint( request.getReorderPoint() );
        inventory.reorderQuantity( request.getReorderQuantity() );

        return inventory.build();
    }

    @Override
    public InventoryResponse toInventoryResponse(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }

        InventoryResponse.InventoryResponseBuilder inventoryResponse = InventoryResponse.builder();

        inventoryResponse.productId( inventoryProductId( inventory ) );
        inventoryResponse.productCode( inventoryProductCode( inventory ) );
        inventoryResponse.productName( inventoryProductName( inventory ) );
        inventoryResponse.branchId( inventory.getBranchId() );
        inventoryResponse.createdAt( inventory.getCreatedAt() );
        inventoryResponse.id( inventory.getId() );
        inventoryResponse.quantityAvailable( inventory.getQuantityAvailable() );
        inventoryResponse.quantityOnHand( inventory.getQuantityOnHand() );
        inventoryResponse.quantityReserved( inventory.getQuantityReserved() );
        inventoryResponse.reorderPoint( inventory.getReorderPoint() );
        inventoryResponse.reorderQuantity( inventory.getReorderQuantity() );
        inventoryResponse.tenantId( inventory.getTenantId() );
        inventoryResponse.updatedAt( inventory.getUpdatedAt() );

        inventoryResponse.isLowStock( isLowStock(inventory) );

        return inventoryResponse.build();
    }

    @Override
    public void updateInventoryFromRequest(InventoryRequest request, Inventory inventory) {
        if ( request == null ) {
            return;
        }

        if ( request.getQuantityOnHand() != null ) {
            inventory.setQuantityOnHand( request.getQuantityOnHand() );
        }
        if ( request.getQuantityReserved() != null ) {
            inventory.setQuantityReserved( request.getQuantityReserved() );
        }
        if ( request.getReorderPoint() != null ) {
            inventory.setReorderPoint( request.getReorderPoint() );
        }
        if ( request.getReorderQuantity() != null ) {
            inventory.setReorderQuantity( request.getReorderQuantity() );
        }
    }

    @Override
    public PriceHistoryResponse toPriceHistoryResponse(PriceHistory priceHistory) {
        if ( priceHistory == null ) {
            return null;
        }

        PriceHistoryResponse.PriceHistoryResponseBuilder priceHistoryResponse = PriceHistoryResponse.builder();

        priceHistoryResponse.productId( priceHistoryProductId( priceHistory ) );
        priceHistoryResponse.productCode( priceHistoryProductCode( priceHistory ) );
        priceHistoryResponse.productName( priceHistoryProductName( priceHistory ) );
        priceHistoryResponse.changedBy( priceHistory.getChangedBy() );
        priceHistoryResponse.createdAt( priceHistory.getCreatedAt() );
        priceHistoryResponse.effectiveFrom( priceHistory.getEffectiveFrom() );
        priceHistoryResponse.effectiveTo( priceHistory.getEffectiveTo() );
        priceHistoryResponse.id( priceHistory.getId() );
        priceHistoryResponse.newPrice( priceHistory.getNewPrice() );
        priceHistoryResponse.oldPrice( priceHistory.getOldPrice() );
        priceHistoryResponse.reason( priceHistory.getReason() );
        priceHistoryResponse.tenantId( priceHistory.getTenantId() );

        return priceHistoryResponse.build();
    }

    private UUID categoryParentId(Category category) {
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        UUID id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String categoryParentName(Category category) {
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        String name = parent.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID productCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        UUID id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID inventoryProductId(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String inventoryProductCode(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        String code = product.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String inventoryProductName(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID priceHistoryProductId(PriceHistory priceHistory) {
        if ( priceHistory == null ) {
            return null;
        }
        Product product = priceHistory.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String priceHistoryProductCode(PriceHistory priceHistory) {
        if ( priceHistory == null ) {
            return null;
        }
        Product product = priceHistory.getProduct();
        if ( product == null ) {
            return null;
        }
        String code = product.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String priceHistoryProductName(PriceHistory priceHistory) {
        if ( priceHistory == null ) {
            return null;
        }
        Product product = priceHistory.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
