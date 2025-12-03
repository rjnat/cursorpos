package com.cursorpos.product.service;

import com.cursorpos.product.dto.PriceHistoryResponse;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.PriceHistoryRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceHistoryServiceTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private PriceHistoryService priceHistoryService;

    private MockedStatic<TenantContext> tenantContextMock;
    private static final String TEST_TENANT = "tenant-test-001";

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void testGetPriceHistory() {
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setId(productId);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setTenantId(TEST_TENANT);
        priceHistory.setProduct(product);
        priceHistory.setOldPrice(BigDecimal.valueOf(90.00));
        priceHistory.setNewPrice(BigDecimal.valueOf(100.00));
        priceHistory.setEffectiveFrom(LocalDateTime.now().minusDays(1));

        PriceHistoryResponse response = PriceHistoryResponse.builder()
                .id(priceHistory.getId())
                .productId(productId)
                .oldPrice(BigDecimal.valueOf(90.00))
                .newPrice(BigDecimal.valueOf(100.00))
                .build();

        @SuppressWarnings("null")
        Page<PriceHistory> page = new PageImpl<>(List.of(priceHistory));

        when(priceHistoryRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(TEST_TENANT, productId, pageable))
                .thenReturn(page);
        when(productMapper.toPriceHistoryResponse(any(PriceHistory.class)))
                .thenReturn(response);

        PagedResponse<PriceHistoryResponse> result = priceHistoryService.getPriceHistory(productId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo(productId);
        verify(priceHistoryRepository).findByTenantIdAndProductIdAndDeletedAtIsNull(TEST_TENANT, productId, pageable);
        verify(productMapper).toPriceHistoryResponse(priceHistory);
    }

    @Test
    void testGetPriceHistory_EmptyPage() {
        UUID productId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        @SuppressWarnings("null")
        Page<PriceHistory> emptyPage = new PageImpl<>(List.of());
        when(priceHistoryRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(TEST_TENANT, productId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<PriceHistoryResponse> result = priceHistoryService.getPriceHistory(productId, pageable);

        assertThat(result.getContent()).isEmpty();
        verify(priceHistoryRepository).findByTenantIdAndProductIdAndDeletedAtIsNull(TEST_TENANT, productId, pageable);
        verify(productMapper, never()).toPriceHistoryResponse(any());
    }

    @Test
    void testGetEffectivePrice_Found() {
        UUID productId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        BigDecimal expectedPrice = BigDecimal.valueOf(100.00);

        Product product = new Product();
        product.setId(productId);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(UUID.randomUUID());
        priceHistory.setTenantId(TEST_TENANT);
        priceHistory.setProduct(product);
        priceHistory.setNewPrice(expectedPrice);
        priceHistory.setEffectiveFrom(date.minusDays(1));

        PriceHistoryResponse response = PriceHistoryResponse.builder()
                .newPrice(expectedPrice)
                .build();

        when(priceHistoryRepository.findEffectivePrice(TEST_TENANT, productId, date))
                .thenReturn(Optional.of(priceHistory));
        when(productMapper.toPriceHistoryResponse(priceHistory))
                .thenReturn(response);

        Optional<PriceHistoryResponse> result = priceHistoryService.getEffectivePrice(productId, date);

        assertThat(result).isPresent();
        assertThat(result.get().getNewPrice()).isEqualByComparingTo(expectedPrice);
        verify(priceHistoryRepository).findEffectivePrice(TEST_TENANT, productId, date);
    }

    @Test
    void testGetEffectivePrice_NotFound() {
        UUID productId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();

        when(priceHistoryRepository.findEffectivePrice(TEST_TENANT, productId, date))
                .thenReturn(Optional.empty());

        Optional<PriceHistoryResponse> result = priceHistoryService.getEffectivePrice(productId, date);

        assertThat(result).isEmpty();
        verify(priceHistoryRepository).findEffectivePrice(TEST_TENANT, productId, date);
    }
}
