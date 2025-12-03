package com.cursorpos.product.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to cover the null entity checks in mapper methods.
 * These test the branches where the entire request/entity is null.
 */
class ProductMapperDeepNullTest {

    private final ProductMapper mapper = new ProductMapperImpl();

    @Test
    void testToCategoryWithNullRequest() {
        var result = mapper.toCategory(null);
        assertThat(result).isNull();
    }

    @Test
    void testToCategoryResponseWithNullEntity() {
        var result = mapper.toCategoryResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToProductWithNullRequest() {
        var result = mapper.toProduct(null);
        assertThat(result).isNull();
    }

    @Test
    void testToProductResponseWithNullEntity() {
        var result = mapper.toProductResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToInventoryWithNullRequest() {
        var result = mapper.toInventory(null);
        assertThat(result).isNull();
    }

    @Test
    void testToInventoryResponseWithNullEntity() {
        var result = mapper.toInventoryResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void testToPriceHistoryResponseWithNullEntity() {
        var result = mapper.toPriceHistoryResponse(null);
        assertThat(result).isNull();
    }
}
