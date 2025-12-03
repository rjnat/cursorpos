package com.cursorpos.product.mapper;

import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.InventoryResponse;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProductMapperInventoryTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void testToInventory() {
        InventoryRequest request = InventoryRequest.builder()
                .productId(UUID.randomUUID())
                .branchId(UUID.randomUUID())
                .quantityOnHand(100)
                .quantityReserved(10)
                .reorderPoint(20)
                .reorderQuantity(50)
                .build();

        Inventory inventory = productMapper.toInventory(request);

        assertThat(inventory).isNotNull();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        assertThat(inventory.getQuantityReserved()).isEqualTo(10);
        assertThat(inventory.getReorderPoint()).isEqualTo(20);
        assertThat(inventory.getReorderQuantity()).isEqualTo(50);
    }

    @Test
    void testToInventoryResponse() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setCode("PROD-001");
        product.setName("Test Product");

        Inventory inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setTenantId("tenant-001");
        inventory.setProduct(product);
        inventory.setBranchId(UUID.randomUUID());
        inventory.setQuantityOnHand(100);
        inventory.setQuantityReserved(10);
        inventory.setQuantityAvailable(90);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);

        InventoryResponse response = productMapper.toInventoryResponse(inventory);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(inventory.getId());
        assertThat(response.getTenantId()).isEqualTo("tenant-001");
        assertThat(response.getProductId()).isEqualTo(product.getId());
        assertThat(response.getProductCode()).isEqualTo("PROD-001");
        assertThat(response.getProductName()).isEqualTo("Test Product");
        assertThat(response.getQuantityOnHand()).isEqualTo(100);
        assertThat(response.getQuantityReserved()).isEqualTo(10);
        assertThat(response.getQuantityAvailable()).isEqualTo(90);
    }

    @Test
    void testUpdateInventoryFromRequest() {
        Product product = new Product();
        product.setId(UUID.randomUUID());

        Inventory inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setTenantId("tenant-001");
        inventory.setProduct(product);
        inventory.setBranchId(UUID.randomUUID());
        inventory.setQuantityOnHand(50);
        inventory.setQuantityReserved(5);

        InventoryRequest request = InventoryRequest.builder()
                .productId(product.getId())
                .branchId(inventory.getBranchId())
                .quantityOnHand(100)
                .quantityReserved(15)
                .reorderPoint(30)
                .reorderQuantity(60)
                .build();

        productMapper.updateInventoryFromRequest(request, inventory);

        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        assertThat(inventory.getQuantityReserved()).isEqualTo(15);
        assertThat(inventory.getReorderPoint()).isEqualTo(30);
        assertThat(inventory.getReorderQuantity()).isEqualTo(60);
    }

    @Test
    void testIsLowStock_BelowReorderPoint() {
        Inventory inventory = new Inventory();
        inventory.setQuantityAvailable(15);
        inventory.setReorderPoint(20);

        boolean isLowStock = productMapper.isLowStock(inventory);

        assertThat(isLowStock).isTrue();
    }

    @Test
    void testIsLowStock_AboveReorderPoint() {
        Inventory inventory = new Inventory();
        inventory.setQuantityAvailable(25);
        inventory.setReorderPoint(20);

        boolean isLowStock = productMapper.isLowStock(inventory);

        assertThat(isLowStock).isFalse();
    }

    @Test
    void testIsLowStock_NullReorderPoint() {
        Inventory inventory = new Inventory();
        inventory.setQuantityAvailable(15);
        inventory.setReorderPoint(null);

        boolean isLowStock = productMapper.isLowStock(inventory);

        assertThat(isLowStock).isFalse();
    }
}
