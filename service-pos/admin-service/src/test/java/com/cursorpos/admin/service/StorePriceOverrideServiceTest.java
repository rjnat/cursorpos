package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.entity.Store;
import com.cursorpos.admin.entity.StorePriceOverride;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.StorePriceOverrideRepository;
import com.cursorpos.admin.repository.StoreRepository;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StorePriceOverrideService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StorePriceOverrideServiceTest {

    @Mock
    private StorePriceOverrideRepository priceOverrideRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private StorePriceOverrideService storePriceOverrideService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = "tenant-test-001";
    private UUID overrideId;
    private UUID storeId;
    private UUID productId;
    private Store store;
    private StorePriceOverride priceOverride;
    private StorePriceOverrideRequest request;
    private StorePriceOverrideResponse response;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

        overrideId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();

        store = new Store();
        store.setId(storeId);
        store.setTenantId(TENANT_ID);
        store.setCode("STORE-001");
        store.setName("Main Store");
        store.setIsActive(true);

        priceOverride = new StorePriceOverride();
        priceOverride.setId(overrideId);
        priceOverride.setTenantId(TENANT_ID);
        priceOverride.setStoreId(storeId);
        priceOverride.setProductId(productId);
        priceOverride.setOverridePrice(BigDecimal.valueOf(9.99));
        priceOverride.setDiscountPercentage(BigDecimal.valueOf(10));
        priceOverride.setEffectiveFrom(Instant.now().minusSeconds(86400));
        priceOverride.setEffectiveTo(Instant.now().plusSeconds(86400L * 30));
        priceOverride.setIsActive(true);

        request = StorePriceOverrideRequest.builder()
                .storeId(storeId)
                .productId(productId)
                .overridePrice(BigDecimal.valueOf(9.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .effectiveFrom(Instant.now().minusSeconds(86400))
                .effectiveTo(Instant.now().plusSeconds(86400L * 30))
                .build();

        response = StorePriceOverrideResponse.builder()
                .id(overrideId)
                .storeId(storeId)
                .productId(productId)
                .overridePrice(BigDecimal.valueOf(9.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .effectiveFrom(Instant.now().minusSeconds(86400))
                .effectiveTo(Instant.now().plusSeconds(86400L * 30))
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("createOverride tests")
    class CreateOverrideTests {

        @Test
        @DisplayName("Should create override successfully")
        void shouldCreateOverrideSuccessfully() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(priceOverrideRepository.existsByTenantIdAndStoreIdAndProductIdAndDeletedAtIsNull(
                    TENANT_ID, storeId, productId)).thenReturn(false);
            when(adminMapper.toStorePriceOverride(request)).thenReturn(priceOverride);
            when(priceOverrideRepository.save(priceOverride)).thenReturn(priceOverride);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            StorePriceOverrideResponse result = storePriceOverrideService.createOverride(request);

            assertThat(result).isNotNull();
            assertThat(result.getOverridePrice()).isEqualTo(BigDecimal.valueOf(9.99));
            verify(priceOverrideRepository).save(priceOverride);
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.createOverride(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Store not found");
        }

        @Test
        @DisplayName("Should throw exception when override already exists")
        void shouldThrowExceptionWhenOverrideExists() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(priceOverrideRepository.existsByTenantIdAndStoreIdAndProductIdAndDeletedAtIsNull(
                    TENANT_ID, storeId, productId)).thenReturn(true);

            assertThatThrownBy(() -> storePriceOverrideService.createOverride(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.createOverride(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getOverrideById tests")
    class GetOverrideByIdTests {

        @Test
        @DisplayName("Should return override when found")
        void shouldReturnOverrideWhenFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.of(priceOverride));
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            StorePriceOverrideResponse result = storePriceOverrideService.getOverrideById(overrideId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(overrideId);
        }

        @Test
        @DisplayName("Should throw exception when override not found")
        void shouldThrowExceptionWhenNotFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.getOverrideById(overrideId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.getOverrideById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getOverridesByStore tests")
    class GetOverridesByStoreTests {

        @Test
        @DisplayName("Should return overrides by store")
        void shouldReturnOverridesByStore() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<StorePriceOverride> page = new PageImpl<>(List.of(priceOverride));

            when(priceOverrideRepository.findByTenantIdAndStoreIdAndDeletedAtIsNull(TENANT_ID, storeId, pageable))
                    .thenReturn(page);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            var result = storePriceOverrideService.getOverridesByStore(storeId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when storeId is null")
        void shouldThrowExceptionWhenStoreIdIsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            assertThatThrownBy(() -> storePriceOverrideService.getOverridesByStore(null, pageable))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getOverridesByProduct tests")
    class GetOverridesByProductTests {

        @Test
        @DisplayName("Should return overrides by product")
        void shouldReturnOverridesByProduct() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<StorePriceOverride> page = new PageImpl<>(List.of(priceOverride));

            when(priceOverrideRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(TENANT_ID, productId, pageable))
                    .thenReturn(page);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            var result = storePriceOverrideService.getOverridesByProduct(productId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when productId is null")
        void shouldThrowExceptionWhenProductIdIsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            assertThatThrownBy(() -> storePriceOverrideService.getOverridesByProduct(null, pageable))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getActiveOverride tests")
    class GetActiveOverrideTests {

        @Test
        @DisplayName("Should return active override")
        void shouldReturnActiveOverride() {
            when(priceOverrideRepository.findActiveOverride(eq(TENANT_ID), eq(storeId), eq(productId),
                    any(Instant.class)))
                    .thenReturn(Optional.of(priceOverride));
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            Optional<StorePriceOverrideResponse> result = storePriceOverrideService.getActiveOverride(storeId,
                    productId);

            assertThat(result).isPresent();
            assertThat(result.get().getOverridePrice()).isEqualTo(BigDecimal.valueOf(9.99));
        }

        @Test
        @DisplayName("Should return empty when no active override")
        void shouldReturnEmptyWhenNoActiveOverride() {
            when(priceOverrideRepository.findActiveOverride(eq(TENANT_ID), eq(storeId), eq(productId),
                    any(Instant.class)))
                    .thenReturn(Optional.empty());

            Optional<StorePriceOverrideResponse> result = storePriceOverrideService.getActiveOverride(storeId,
                    productId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when storeId is null")
        void shouldThrowExceptionWhenStoreIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.getActiveOverride(null, productId))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when productId is null")
        void shouldThrowExceptionWhenProductIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.getActiveOverride(storeId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getAllActiveOverridesForStore tests")
    class GetAllActiveOverridesForStoreTests {

        @Test
        @DisplayName("Should return all active overrides for store")
        void shouldReturnAllActiveOverridesForStore() {
            when(priceOverrideRepository.findAllActiveOverridesForStore(eq(TENANT_ID), eq(storeId),
                    any(Instant.class)))
                    .thenReturn(List.of(priceOverride));
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            List<StorePriceOverrideResponse> result = storePriceOverrideService.getAllActiveOverridesForStore(storeId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when storeId is null")
        void shouldThrowExceptionWhenStoreIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.getAllActiveOverridesForStore(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("updateOverride tests")
    class UpdateOverrideTests {

        @Test
        @DisplayName("Should update override successfully")
        void shouldUpdateOverrideSuccessfully() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.of(priceOverride));
            when(priceOverrideRepository.save(priceOverride)).thenReturn(priceOverride);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            StorePriceOverrideResponse result = storePriceOverrideService.updateOverride(overrideId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateStorePriceOverrideFromRequest(request, priceOverride);
            verify(priceOverrideRepository).save(priceOverride);
        }

        @Test
        @DisplayName("Should throw exception when override not found")
        void shouldThrowExceptionWhenOverrideNotFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.updateOverride(overrideId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.updateOverride(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.updateOverride(overrideId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteOverride tests")
    class DeleteOverrideTests {

        @Test
        @DisplayName("Should soft delete override successfully")
        void shouldSoftDeleteOverrideSuccessfully() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.of(priceOverride));
            when(priceOverrideRepository.save(priceOverride)).thenReturn(priceOverride);

            storePriceOverrideService.deleteOverride(overrideId);

            verify(priceOverrideRepository).save(priceOverride);
        }

        @Test
        @DisplayName("Should throw exception when override not found")
        void shouldThrowExceptionWhenOverrideNotFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.deleteOverride(overrideId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.deleteOverride(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activateOverride tests")
    class ActivateOverrideTests {

        @Test
        @DisplayName("Should activate override successfully")
        void shouldActivateOverrideSuccessfully() {
            priceOverride.setIsActive(false);
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.of(priceOverride));
            when(priceOverrideRepository.save(priceOverride)).thenReturn(priceOverride);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            StorePriceOverrideResponse result = storePriceOverrideService.activateOverride(overrideId);

            assertThat(result).isNotNull();
            assertThat(priceOverride.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when override not found")
        void shouldThrowExceptionWhenOverrideNotFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.activateOverride(overrideId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.activateOverride(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivateOverride tests")
    class DeactivateOverrideTests {

        @Test
        @DisplayName("Should deactivate override successfully")
        void shouldDeactivateOverrideSuccessfully() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.of(priceOverride));
            when(priceOverrideRepository.save(priceOverride)).thenReturn(priceOverride);
            when(adminMapper.toStorePriceOverrideResponse(priceOverride)).thenReturn(response);

            StorePriceOverrideResponse result = storePriceOverrideService.deactivateOverride(overrideId);

            assertThat(result).isNotNull();
            assertThat(priceOverride.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when override not found")
        void shouldThrowExceptionWhenOverrideNotFound() {
            when(priceOverrideRepository.findByIdAndTenantIdAndDeletedAtIsNull(overrideId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storePriceOverrideService.deactivateOverride(overrideId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storePriceOverrideService.deactivateOverride(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
