package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.entity.Branch;
import com.cursorpos.admin.entity.Store;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.BranchRepository;
import com.cursorpos.admin.repository.StoreRepository;
import com.cursorpos.shared.dto.PagedResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private StoreService storeService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = "tenant-test-001";
    private UUID storeId;
    private UUID branchId;
    private Store store;
    private Branch branch;
    private CreateStoreRequest request;
    private StoreResponse response;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

        storeId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        branch = new Branch();
        branch.setId(branchId);
        branch.setTenantId(TENANT_ID);
        branch.setCode("BRANCH-001");
        branch.setName("Main Branch");
        branch.setIsActive(true);

        store = new Store();
        store.setId(storeId);
        store.setTenantId(TENANT_ID);
        store.setBranchId(branchId);
        store.setCode("STORE-001");
        store.setName("Main Store");
        store.setDescription("Main store location");
        store.setAddress("456 Store St");
        store.setCity("New York");
        store.setState("NY");
        store.setCountry("USA");
        store.setPostalCode("10002");
        store.setPhone("555-9999");
        store.setEmail("store@example.com");
        store.setIsActive(true);
        store.setGlobalDiscountPercentage(BigDecimal.valueOf(5.0));

        request = CreateStoreRequest.builder()
                .branchId(branchId)
                .code("STORE-001")
                .name("Main Store")
                .description("Main store location")
                .address("456 Store St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10002")
                .phone("555-9999")
                .email("store@example.com")
                .globalDiscountPercentage(BigDecimal.valueOf(5.0))
                .build();

        response = StoreResponse.builder()
                .id(storeId)
                .branchId(branchId)
                .code("STORE-001")
                .name("Main Store")
                .description("Main store location")
                .address("456 Store St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10002")
                .phone("555-9999")
                .email("store@example.com")
                .isActive(true)
                .globalDiscountPercentage(BigDecimal.valueOf(5.0))
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("createStore tests")
    class CreateStoreTests {

        @Test
        @DisplayName("Should create store successfully")
        void shouldCreateStoreSuccessfully() {
            when(storeRepository.existsByTenantIdAndCode(TENANT_ID, "STORE-001")).thenReturn(false);
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(adminMapper.toStore(request)).thenReturn(store);
            when(storeRepository.save(store)).thenReturn(store);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.createStore(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("STORE-001");
            verify(storeRepository).save(store);
        }

        @Test
        @DisplayName("Should throw exception when store code already exists")
        void shouldThrowExceptionWhenStoreCodeExists() {
            when(storeRepository.existsByTenantIdAndCode(TENANT_ID, "STORE-001")).thenReturn(true);

            assertThatThrownBy(() -> storeService.createStore(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenBranchNotFound() {
            when(storeRepository.existsByTenantIdAndCode(TENANT_ID, "STORE-001")).thenReturn(false);
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.createStore(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Branch not found");
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> storeService.createStore(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getStoreById tests")
    class GetStoreByIdTests {

        @Test
        @DisplayName("Should return store when found")
        void shouldReturnStoreWhenFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.getStoreById(storeId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(storeId);
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.getStoreById(storeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storeService.getStoreById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getStoreByCode tests")
    class GetStoreByCodeTests {

        @Test
        @DisplayName("Should return store when found by code")
        void shouldReturnStoreWhenFoundByCode() {
            when(storeRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "STORE-001"))
                    .thenReturn(Optional.of(store));
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.getStoreByCode("STORE-001");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("STORE-001");
        }

        @Test
        @DisplayName("Should throw exception when store not found by code")
        void shouldThrowExceptionWhenNotFoundByCode() {
            when(storeRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.getStoreByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllStores tests")
    class GetAllStoresTests {

        @Test
        @DisplayName("Should return all stores paginated")
        void shouldReturnAllStoresPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Store> page = new PageImpl<>(List.of(store));

            when(storeRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable)).thenReturn(page);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            PagedResponse<StoreResponse> result = storeService.getAllStores(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getStoresByBranch tests")
    class GetStoresByBranchTests {

        @Test
        @DisplayName("Should return stores by branch paginated")
        void shouldReturnStoresByBranchPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Store> page = new PageImpl<>(List.of(store));

            when(storeRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(TENANT_ID, branchId, pageable))
                    .thenReturn(page);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            PagedResponse<StoreResponse> result = storeService.getStoresByBranch(branchId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getActiveStoresByBranch tests")
    class GetActiveStoresByBranchTests {

        @Test
        @DisplayName("Should return active stores by branch")
        void shouldReturnActiveStoresByBranch() {
            when(storeRepository.findByTenantIdAndBranchIdAndIsActiveAndDeletedAtIsNull(
                    TENANT_ID, branchId, true))
                    .thenReturn(List.of(store));
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            List<StoreResponse> result = storeService.getActiveStoresByBranch(branchId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateStore tests")
    class UpdateStoreTests {

        @Test
        @DisplayName("Should update store successfully")
        void shouldUpdateStoreSuccessfully() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(storeRepository.save(store)).thenReturn(store);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.updateStore(storeId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateStoreFromRequest(request, store);
            verify(storeRepository).save(store);
        }

        @Test
        @DisplayName("Should update store with different branch successfully")
        void shouldUpdateStoreWithDifferentBranchSuccessfully() {
            UUID newBranchId = UUID.randomUUID();
            Branch newBranch = new Branch();
            newBranch.setId(newBranchId);
            newBranch.setTenantId(TENANT_ID);
            CreateStoreRequest updateRequest = CreateStoreRequest.builder()
                    .branchId(newBranchId)
                    .code("STORE-001")
                    .name("Main Store")
                    .build();

            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(newBranchId, TENANT_ID))
                    .thenReturn(Optional.of(newBranch));
            when(storeRepository.save(store)).thenReturn(store);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.updateStore(storeId, updateRequest);

            assertThat(result).isNotNull();
            verify(branchRepository).findByIdAndTenantIdAndDeletedAtIsNull(newBranchId, TENANT_ID);
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.updateStore(storeId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when new branch not found")
        void shouldThrowExceptionWhenNewBranchNotFound() {
            UUID newBranchId = UUID.randomUUID();
            CreateStoreRequest updateRequest = CreateStoreRequest.builder()
                    .branchId(newBranchId)
                    .code("STORE-001")
                    .name("Main Store")
                    .build();

            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(newBranchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.updateStore(storeId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Branch not found");
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storeService.updateStore(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> storeService.updateStore(storeId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteStore tests")
    class DeleteStoreTests {

        @Test
        @DisplayName("Should soft delete store successfully")
        void shouldSoftDeleteStoreSuccessfully() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(storeRepository.save(store)).thenReturn(store);

            storeService.deleteStore(storeId);

            verify(storeRepository).save(store);
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.deleteStore(storeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storeService.deleteStore(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activateStore tests")
    class ActivateStoreTests {

        @Test
        @DisplayName("Should activate store successfully")
        void shouldActivateStoreSuccessfully() {
            store.setIsActive(false);
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(storeRepository.save(store)).thenReturn(store);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.activateStore(storeId);

            assertThat(result).isNotNull();
            assertThat(store.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.activateStore(storeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storeService.activateStore(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivateStore tests")
    class DeactivateStoreTests {

        @Test
        @DisplayName("Should deactivate store successfully")
        void shouldDeactivateStoreSuccessfully() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.of(store));
            when(storeRepository.save(store)).thenReturn(store);
            when(adminMapper.toStoreResponse(store)).thenReturn(response);

            StoreResponse result = storeService.deactivateStore(storeId);

            assertThat(result).isNotNull();
            assertThat(store.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            when(storeRepository.findByIdAndTenantIdAndDeletedAtIsNull(storeId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeService.deactivateStore(storeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> storeService.deactivateStore(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
