package com.cursorpos.product.mapper;

import com.cursorpos.product.dto.*;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import org.mapstruct.*;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Product domain entities and DTOs.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // Category mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toCategory(CategoryRequest request);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    void updateCategoryFromRequest(CategoryRequest request, @MappingTarget Category category);

    // Product mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toProduct(ProductRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);

    // Inventory mappings
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "quantityAvailable", ignore = true)
    Inventory toInventory(InventoryRequest request);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.code", target = "productCode")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "isLowStock", expression = "java(isLowStock(inventory))")
    InventoryResponse toInventoryResponse(Inventory inventory);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "quantityAvailable", ignore = true)
    void updateInventoryFromRequest(InventoryRequest request, @MappingTarget Inventory inventory);

    // PriceHistory mappings
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.code", target = "productCode")
    @Mapping(source = "product.name", target = "productName")
    PriceHistoryResponse toPriceHistoryResponse(PriceHistory priceHistory);

    // Helper methods
    default boolean isLowStock(Inventory inventory) {
        if (inventory.getReorderPoint() == null) {
            return false;
        }
        return inventory.getQuantityAvailable() != null &&
                inventory.getQuantityAvailable() < inventory.getReorderPoint();
    }
}
