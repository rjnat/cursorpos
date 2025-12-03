package com.cursorpos.product.service;

import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.InventoryResponse;
import com.cursorpos.product.dto.StockAdjustmentRequest;
import com.cursorpos.product.dto.StockAdjustmentRequest.AdjustmentType;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.mapper.ProductMapper;
import com.cursorpos.product.repository.InventoryRepository;
import com.cursorpos.product.repository.ProductRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService.
 * Tests business logic in isolation using mocked dependencies.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class InventoryServiceTest {

        @Mock
        private InventoryRepository inventoryRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private ProductMapper productMapper;

        @InjectMocks
        private InventoryService inventoryService;

        private MockedStatic<TenantContext> tenantContextMock;
        private static final String TEST_TENANT = "tenant-test-001";
        private UUID inventoryId;
        private UUID productId;
        private UUID branchId;
        private Inventory inventory;
        private Product product;
        private InventoryRequest inventoryRequest;
        private InventoryResponse inventoryResponse;

        @BeforeEach
        void setUp() {
                tenantContextMock = mockStatic(TenantContext.class);
                tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT);

                inventoryId = UUID.randomUUID();
                productId = UUID.randomUUID();
                branchId = UUID.randomUUID();

                product = new Product();
                product.setId(productId);
                product.setTenantId(TEST_TENANT);
                product.setCode("ESPRESSO");
                product.setName("Espresso");

                inventory = new Inventory();
                inventory.setId(inventoryId);
                inventory.setTenantId(TEST_TENANT);
                inventory.setProduct(product);
                inventory.setBranchId(branchId);
                inventory.setQuantityOnHand(100);
                inventory.setQuantityReserved(10);
                inventory.setReorderPoint(20);
                inventory.setReorderQuantity(50);

                inventoryRequest = InventoryRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .quantityOnHand(100)
                                .quantityReserved(10)
                                .reorderPoint(20)
                                .reorderQuantity(50)
                                .build();

                inventoryResponse = InventoryResponse.builder()
                                .id(inventoryId)
                                .tenantId(TEST_TENANT)
                                .productId(productId)
                                .branchId(branchId)
                                .quantityOnHand(100)
                                .quantityReserved(10)
                                .quantityAvailable(90)
                                .reorderPoint(20)
                                .reorderQuantity(50)
                                .build();
        }

        @AfterEach
        void tearDown() {
                tenantContextMock.close();
        }

        @Test
        @DisplayName("Should create new inventory successfully")
        void testCreateInventory() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.empty());
                when(productMapper.toInventory(inventoryRequest)).thenReturn(inventory);
                when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.createOrUpdateInventory(inventoryRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getProductId()).isEqualTo(productId);
                assertThat(result.getBranchId()).isEqualTo(branchId);

                verify(productRepository).findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT);
                verify(inventoryRepository).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should update existing inventory successfully")
        void testUpdateInventory() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.of(product));
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                doNothing().when(productMapper).updateInventoryFromRequest(inventoryRequest, inventory);
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.createOrUpdateInventory(inventoryRequest);

                // Then
                assertThat(result).isNotNull();

                verify(productMapper).updateInventoryFromRequest(inventoryRequest, inventory);
                verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testCreateInventoryProductNotFound() {
                // Given
                when(productRepository.findByIdAndTenantIdAndDeletedAtIsNull(productId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.createOrUpdateInventory(inventoryRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should adjust stock with ADD type successfully")
        void testAdjustStockAdd() {
                // Given
                StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .type(AdjustmentType.ADD)
                                .quantity(50)
                                .reason("Purchase order received")
                                .build();

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.adjustStock(request);

                // Then
                assertThat(result).isNotNull();
                assertThat(inventory.getQuantityOnHand()).isEqualTo(150);

                verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should adjust stock with SUBTRACT type successfully")
        void testAdjustStockSubtract() {
                // Given
                StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .type(AdjustmentType.SUBTRACT)
                                .quantity(30)
                                .reason("Damaged goods")
                                .build();

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.adjustStock(request);

                // Then
                assertThat(result).isNotNull();
                assertThat(inventory.getQuantityOnHand()).isEqualTo(70);

                verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should throw exception when subtracting more than available")
        void testAdjustStockSubtractInsufficientStock() {
                // Given
                StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .type(AdjustmentType.SUBTRACT)
                                .quantity(150)
                                .build();

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));

                // When/Then
                assertThatThrownBy(() -> inventoryService.adjustStock(request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Insufficient stock");

                verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should adjust stock with SET type successfully")
        void testAdjustStockSet() {
                // Given
                StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .type(AdjustmentType.SET)
                                .quantity(75)
                                .reason("Physical count adjustment")
                                .build();

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.adjustStock(request);

                // Then
                assertThat(result).isNotNull();
                assertThat(inventory.getQuantityOnHand()).isEqualTo(75);
        }

        @Test
        @DisplayName("Should reserve stock successfully")
        void testReserveStock() {
                // Given
                inventory.setQuantityOnHand(100);
                inventory.setQuantityReserved(10);
                inventory.calculateAvailableQuantity(); // Calculate available = 100 - 10 = 90

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse); // When
                InventoryResponse result = inventoryService.reserveStock(productId, branchId, 20);

                // Then
                assertThat(result).isNotNull();
                assertThat(inventory.getQuantityReserved()).isEqualTo(30);

                verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should throw exception when reserving more than available")
        void testReserveStockInsufficientAvailable() {
                // Given
                inventory.setQuantityOnHand(100);
                inventory.setQuantityReserved(10);

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));

                // When/Then
                assertThatThrownBy(() -> inventoryService.reserveStock(productId, branchId, 100))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Insufficient stock available");
        }

        @Test
        @DisplayName("Should release stock successfully")
        void testReleaseStock() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(inventoryRepository.save(inventory)).thenReturn(inventory);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.releaseStock(productId, branchId, 5);

                // Then
                assertThat(result).isNotNull();
                assertThat(inventory.getQuantityReserved()).isEqualTo(5);

                verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should throw exception when releasing more than reserved")
        void testReleaseStockExceedsReserved() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));

                // When/Then
                assertThatThrownBy(() -> inventoryService.releaseStock(productId, branchId, 20))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Cannot release more than reserved");
        }

        @Test
        @DisplayName("Should get inventory by ID successfully")
        void testGetInventoryById() {
                // Given
                when(inventoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(inventoryId, TEST_TENANT))
                                .thenReturn(Optional.of(inventory));
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.getInventoryById(inventoryId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(inventoryId);

                verify(inventoryRepository).findByIdAndTenantIdAndDeletedAtIsNull(inventoryId, TEST_TENANT);
        }

        @Test
        @DisplayName("Should throw exception when inventory not found by ID")
        void testGetInventoryByIdNotFound() {
                // Given
                when(inventoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(inventoryId, TEST_TENANT))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.getInventoryById(inventoryId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Inventory not found");
        }

        @Test
        @DisplayName("Should get inventory by product and branch successfully")
        void testGetInventoryByProductAndBranch() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(
                                TEST_TENANT, productId, branchId))
                                .thenReturn(Optional.of(inventory));
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                InventoryResponse result = inventoryService.getInventoryByProductAndBranch(productId, branchId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getProductId()).isEqualTo(productId);
                assertThat(result.getBranchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should get all inventory with pagination")
        void testGetAllInventory() {
                // Given
                Inventory inventory2 = new Inventory();
                inventory2.setId(UUID.randomUUID());

                List<Inventory> inventories = Arrays.asList(inventory, inventory2);
                Page<Inventory> page = new PageImpl<>(inventories, PageRequest.of(0, 10), inventories.size());

                when(inventoryRepository.findByTenantIdAndDeletedAtIsNull(eq(TEST_TENANT), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toInventoryResponse(any(Inventory.class)))
                                .thenReturn(inventoryResponse);

                // When
                PagedResponse<InventoryResponse> result = inventoryService.getAllInventory(PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should get inventory by branch")
        void testGetInventoryByBranch() {
                // Given
                List<Inventory> inventories = Arrays.asList(inventory);
                Page<Inventory> page = new PageImpl<>(inventories, PageRequest.of(0, 10), inventories.size());

                when(inventoryRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(
                                eq(TEST_TENANT), eq(branchId), any(Pageable.class)))
                                .thenReturn(page);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                PagedResponse<InventoryResponse> result = inventoryService.getInventoryByBranch(branchId,
                                PageRequest.of(0, 10));

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get inventory by product")
        void testGetInventoryByProduct() {
                // Given
                List<Inventory> inventories = Arrays.asList(inventory);

                when(inventoryRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(TEST_TENANT, productId))
                                .thenReturn(inventories);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                List<InventoryResponse> result = inventoryService.getInventoryByProduct(productId);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getProductId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("Should get low stock items")
        void testGetLowStockItems() {
                // Given
                List<Inventory> lowStockInventories = Arrays.asList(inventory);

                when(inventoryRepository.findLowStockItems(TEST_TENANT))
                                .thenReturn(lowStockInventories);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                List<InventoryResponse> result = inventoryService.getLowStockItems();

                // Then
                assertThat(result).hasSize(1);

                verify(inventoryRepository).findLowStockItems(TEST_TENANT);
        }

        @Test
        @DisplayName("Should get low stock items by branch")
        void testGetLowStockItemsByBranch() {
                // Given
                List<Inventory> lowStockInventories = Arrays.asList(inventory);

                when(inventoryRepository.findLowStockItemsByBranch(TEST_TENANT, branchId))
                                .thenReturn(lowStockInventories);
                when(productMapper.toInventoryResponse(inventory)).thenReturn(inventoryResponse);

                // When
                List<InventoryResponse> result = inventoryService.getLowStockItemsByBranch(branchId);

                // Then
                assertThat(result).hasSize(1);

                verify(inventoryRepository).findLowStockItemsByBranch(TEST_TENANT, branchId);
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void testCreateInventoryNullRequest() {
                // When/Then
                assertThatThrownBy(() -> inventoryService.createOrUpdateInventory(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when ID is null")
        void testGetInventoryByIdNull() {
                // When/Then
                assertThatThrownBy(() -> inventoryService.getInventoryById(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when adjusting non-existent inventory")
        void testAdjustStock_InventoryNotFound() {
                // Given
                StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                                .productId(productId)
                                .branchId(branchId)
                                .type(AdjustmentType.ADD)
                                .quantity(10)
                                .reason("Test")
                                .build();

                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT, productId,
                                branchId))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.adjustStock(request))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Inventory not found");

                verify(inventoryRepository).findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT,
                                productId, branchId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when reserving stock for non-existent inventory")
        void testReserveStock_InventoryNotFound() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT, productId,
                                branchId))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.reserveStock(productId, branchId, 10))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Inventory not found");

                verify(inventoryRepository).findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT,
                                productId, branchId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when releasing stock for non-existent inventory")
        void testReleaseStock_InventoryNotFound() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT, productId,
                                branchId))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.releaseStock(productId, branchId, 5))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Inventory not found");

                verify(inventoryRepository).findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT,
                                productId, branchId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when getting inventory by product and branch - not found")
        void testGetInventoryByProductAndBranch_NotFound() {
                // Given
                when(inventoryRepository.findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT, productId,
                                branchId))
                                .thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> inventoryService.getInventoryByProductAndBranch(productId, branchId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Inventory not found");

                verify(inventoryRepository).findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(TEST_TENANT,
                                productId, branchId);
        }
}
