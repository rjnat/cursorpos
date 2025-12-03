package com.cursorpos.product.mapper;

import com.cursorpos.product.dto.PriceHistoryResponse;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProductMapperPriceHistoryTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void testToPriceHistoryResponse() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setCode("PROD-001");
        product.setName("Test Product");

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setTenantId("tenant-001");
        priceHistory.setProduct(product);
        priceHistory.setOldPrice(BigDecimal.valueOf(100.00));
        priceHistory.setNewPrice(BigDecimal.valueOf(90.00));
        priceHistory.setEffectiveFrom(LocalDateTime.now());
        priceHistory.setChangedBy("admin");
        priceHistory.setReason("Price reduction");

        PriceHistoryResponse response = productMapper.toPriceHistoryResponse(priceHistory);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(priceHistory.getId());
        assertThat(response.getProductId()).isEqualTo(product.getId());
        assertThat(response.getProductCode()).isEqualTo("PROD-001");
        assertThat(response.getProductName()).isEqualTo("Test Product");
        assertThat(response.getOldPrice()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(response.getNewPrice()).isEqualTo(BigDecimal.valueOf(90.00));
        assertThat(response.getChangedBy()).isEqualTo("admin");
        assertThat(response.getReason()).isEqualTo("Price reduction");
    }

    @Test
    void testToPriceHistoryResponse_NullProduct() {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setTenantId("tenant-001");
        priceHistory.setProduct(null);
        priceHistory.setNewPrice(BigDecimal.valueOf(90.00));

        PriceHistoryResponse response = productMapper.toPriceHistoryResponse(priceHistory);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isNull();
        assertThat(response.getProductCode()).isNull();
        assertThat(response.getProductName()).isNull();
    }
}
