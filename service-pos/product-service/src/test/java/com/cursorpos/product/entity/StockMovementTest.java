package com.cursorpos.product.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StockMovement entity.
 * Tests Lombok-generated methods, custom business logic, and enum values.
 */
class StockMovementTest {

    private StockMovement movement1;
    private StockMovement movement2;
    private Product product;
    private UUID storeId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        // Create a test product
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setCode("PROD-001");
        product.setSku("SKU-001");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setTenantId(tenantId.toString());

        // Create test movements
        movement1 = new StockMovement();
        movement1.setId(UUID.randomUUID());
        movement1.setStoreId(storeId);
        movement1.setProduct(product);
        movement1.setMovementType(StockMovement.MovementType.RESTOCK);
        movement1.setQuantityDelta(50);
        movement1.setQuantityAfter(50);
        movement1.setTenantId(tenantId.toString());

        movement2 = new StockMovement();
        movement2.setId(UUID.randomUUID());
        movement2.setStoreId(storeId);
        movement2.setProduct(product);
        movement2.setMovementType(StockMovement.MovementType.RESTOCK);
        movement2.setQuantityDelta(50);
        movement2.setQuantityAfter(50);
        movement2.setTenantId(tenantId.toString());
    }

    @Test
    void testAllArgsConstructor() {
        StockMovement movement = new StockMovement(
                storeId,
                product,
                StockMovement.MovementType.PURCHASE,
                100,
                100,
                "REF-001",
                UUID.randomUUID(),
                "Test notes");

        assertThat(movement.getStoreId()).isEqualTo(storeId);
        assertThat(movement.getProduct()).isEqualTo(product);
        assertThat(movement.getMovementType()).isEqualTo(StockMovement.MovementType.PURCHASE);
        assertThat(movement.getQuantityDelta()).isEqualTo(100);
        assertThat(movement.getQuantityAfter()).isEqualTo(100);
        assertThat(movement.getReferenceNumber()).isEqualTo("REF-001");
        assertThat(movement.getNotes()).isEqualTo("Test notes");
    }

    @Test
    void testNoArgsConstructor() {
        StockMovement movement = new StockMovement();
        assertThat(movement).isNotNull();
        assertThat(movement.getStoreId()).isNull();
        assertThat(movement.getProduct()).isNull();
        assertThat(movement.getMovementType()).isNull();
    }

    @Test
    void testGettersAndSetters() {
        StockMovement movement = new StockMovement();
        UUID orderId = UUID.randomUUID();

        movement.setStoreId(storeId);
        movement.setProduct(product);
        movement.setMovementType(StockMovement.MovementType.SALE);
        movement.setQuantityDelta(-10);
        movement.setQuantityAfter(40);
        movement.setReferenceNumber("SALE-001");
        movement.setReferenceOrderId(orderId);
        movement.setNotes("Customer purchase");

        assertThat(movement.getStoreId()).isEqualTo(storeId);
        assertThat(movement.getProduct()).isEqualTo(product);
        assertThat(movement.getMovementType()).isEqualTo(StockMovement.MovementType.SALE);
        assertThat(movement.getQuantityDelta()).isEqualTo(-10);
        assertThat(movement.getQuantityAfter()).isEqualTo(40);
        assertThat(movement.getReferenceNumber()).isEqualTo("SALE-001");
        assertThat(movement.getReferenceOrderId()).isEqualTo(orderId);
        assertThat(movement.getNotes()).isEqualTo("Customer purchase");
    }

    @Test
    void testIsIncrease_PositiveDelta() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(50);

        assertThat(movement.isIncrease()).isTrue();
    }

    @Test
    void testIsIncrease_NegativeDelta() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(-30);

        assertThat(movement.isIncrease()).isFalse();
    }

    @Test
    void testIsDecrease_NegativeDelta() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(-30);

        assertThat(movement.isDecrease()).isTrue();
    }

    @Test
    void testIsDecrease_PositiveDelta() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(50);

        assertThat(movement.isDecrease()).isFalse();
    }

    @Test
    void testGetAbsoluteChange_PositiveQuantity() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(50);

        assertThat(movement.getAbsoluteChange()).isEqualTo(50);
    }

    @Test
    void testGetAbsoluteChange_NegativeQuantity() {
        StockMovement movement = new StockMovement();
        movement.setQuantityDelta(-30);

        assertThat(movement.getAbsoluteChange()).isEqualTo(30);
    }

    @Test
    void testIsIncrease_Zero() {
        movement1.setQuantityDelta(0);
        assertThat(movement1.isIncrease()).isFalse();
        assertThat(movement1.isDecrease()).isFalse();
    }

    @Test
    void testGetAbsoluteChange_Zero() {
        movement1.setQuantityDelta(0);
        assertThat(movement1.getAbsoluteChange()).isZero();
    }

    @Test
    void testEquals_SameObject() {
        assertThat(movement1.equals(movement1)).isTrue();
    }

    @Test
    void testEquals_Null() {
        assertThat(movement1.equals(null)).isFalse();
    }

    @Test
    @SuppressWarnings("unlikely-arg-type")
    void testEquals_DifferentClass() {
        assertThat(movement1.equals("not a movement")).isFalse();
    }

    @Test
    void testEquals_SameValues() {
        movement2.setId(movement1.getId());
        movement2.setStoreId(movement1.getStoreId());
        movement2.setProduct(movement1.getProduct());
        movement2.setMovementType(movement1.getMovementType());
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1.equals(movement2)).isTrue();
    }

    @Test
    void testEquals_DifferentStoreId() {
        movement2.setId(movement1.getId());
        movement2.setStoreId(UUID.randomUUID()); // Different
        movement2.setProduct(movement1.getProduct());
        movement2.setMovementType(movement1.getMovementType());
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1.equals(movement2)).isFalse();
    }

    @Test
    void testEquals_DifferentProduct() {
        Product differentProduct = new Product();
        differentProduct.setId(UUID.randomUUID());
        differentProduct.setCode("PROD-002");

        movement2.setId(movement1.getId());
        movement2.setStoreId(movement1.getStoreId());
        movement2.setProduct(differentProduct); // Different
        movement2.setMovementType(movement1.getMovementType());
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1.equals(movement2)).isFalse();
    }

    @Test
    void testEquals_DifferentMovementType() {
        movement2.setId(movement1.getId());
        movement2.setStoreId(movement1.getStoreId());
        movement2.setProduct(movement1.getProduct());
        movement2.setMovementType(StockMovement.MovementType.SALE); // Different
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1.equals(movement2)).isFalse();
    }

    @Test
    void testEquals_DifferentBaseEntityFields() {
        movement2.setId(UUID.randomUUID()); // Different ID
        movement2.setStoreId(movement1.getStoreId());
        movement2.setProduct(movement1.getProduct());
        movement2.setMovementType(movement1.getMovementType());
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1.equals(movement2)).isFalse();
    }

    @Test
    void testHashCode_Consistency() {
        int hash1 = movement1.hashCode();
        int hash2 = movement1.hashCode();
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void testHashCode_EqualObjects() {
        movement2.setId(movement1.getId());
        movement2.setStoreId(movement1.getStoreId());
        movement2.setProduct(movement1.getProduct());
        movement2.setMovementType(movement1.getMovementType());
        movement2.setTenantId(movement1.getTenantId());

        assertThat(movement1).hasSameHashCodeAs(movement2);
    }

    @Test
    void testHashCode_DifferentObjects() {
        movement2.setStoreId(UUID.randomUUID());
        assertThat(movement1.hashCode()).isNotEqualTo(movement2.hashCode());
    }

    @Test
    void testMovementType_AllEnumValues() {
        assertThat(StockMovement.MovementType.values()).containsExactlyInAnyOrder(
                StockMovement.MovementType.RESTOCK,
                StockMovement.MovementType.PURCHASE,
                StockMovement.MovementType.SALE,
                StockMovement.MovementType.RETURN,
                StockMovement.MovementType.TRANSFER_OUT,
                StockMovement.MovementType.TRANSFER_IN,
                StockMovement.MovementType.DAMAGE,
                StockMovement.MovementType.LOSS,
                StockMovement.MovementType.ADJUSTMENT,
                StockMovement.MovementType.RESERVE,
                StockMovement.MovementType.RELEASE,
                StockMovement.MovementType.INVENTORY_COUNT);
    }

    @Test
    void testMovementType_ValueOf() {
        assertThat(StockMovement.MovementType.valueOf("RESTOCK")).isEqualTo(StockMovement.MovementType.RESTOCK);
        assertThat(StockMovement.MovementType.valueOf("PURCHASE")).isEqualTo(StockMovement.MovementType.PURCHASE);
        assertThat(StockMovement.MovementType.valueOf("SALE")).isEqualTo(StockMovement.MovementType.SALE);
        assertThat(StockMovement.MovementType.valueOf("RETURN")).isEqualTo(StockMovement.MovementType.RETURN);
        assertThat(StockMovement.MovementType.valueOf("TRANSFER_OUT"))
                .isEqualTo(StockMovement.MovementType.TRANSFER_OUT);
        assertThat(StockMovement.MovementType.valueOf("TRANSFER_IN")).isEqualTo(StockMovement.MovementType.TRANSFER_IN);
        assertThat(StockMovement.MovementType.valueOf("DAMAGE")).isEqualTo(StockMovement.MovementType.DAMAGE);
        assertThat(StockMovement.MovementType.valueOf("LOSS")).isEqualTo(StockMovement.MovementType.LOSS);
        assertThat(StockMovement.MovementType.valueOf("ADJUSTMENT")).isEqualTo(StockMovement.MovementType.ADJUSTMENT);
        assertThat(StockMovement.MovementType.valueOf("RESERVE")).isEqualTo(StockMovement.MovementType.RESERVE);
        assertThat(StockMovement.MovementType.valueOf("RELEASE")).isEqualTo(StockMovement.MovementType.RELEASE);
        assertThat(StockMovement.MovementType.valueOf("INVENTORY_COUNT"))
                .isEqualTo(StockMovement.MovementType.INVENTORY_COUNT);
    }

    @Test
    void testMovementType_EnumToString() {
        assertThat(StockMovement.MovementType.RESTOCK).hasToString("RESTOCK");
        assertThat(StockMovement.MovementType.SALE).hasToString("SALE");
        assertThat(StockMovement.MovementType.INVENTORY_COUNT).hasToString("INVENTORY_COUNT");
    }
}
