package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.LoyaltyTierRequest;
import com.cursorpos.admin.dto.LoyaltyTierResponse;
import com.cursorpos.admin.dto.LoyaltyTransactionRequest;
import com.cursorpos.admin.dto.LoyaltyTransactionResponse;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.entity.LoyaltyTier;
import com.cursorpos.admin.entity.LoyaltyTransaction;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.CustomerRepository;
import com.cursorpos.admin.repository.LoyaltyTierRepository;
import com.cursorpos.admin.repository.LoyaltyTransactionRepository;
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
 * Unit tests for LoyaltyService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class LoyaltyServiceTest {

        @Mock
        private LoyaltyTierRepository loyaltyTierRepository;

        @Mock
        private LoyaltyTransactionRepository loyaltyTransactionRepository;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private AdminMapper adminMapper;

        @InjectMocks
        private LoyaltyService loyaltyService;

        private MockedStatic<TenantContext> tenantContextMock;

        private static final String TENANT_ID = "tenant-test-001";
        private UUID tierId;
        private UUID customerId;
        private UUID transactionId;
        private LoyaltyTier loyaltyTier;
        private Customer customer;
        private LoyaltyTransaction transaction;
        private LoyaltyTierRequest tierRequest;
        private LoyaltyTierResponse tierResponse;
        private LoyaltyTransactionRequest transactionRequest;
        private LoyaltyTransactionResponse transactionResponse;

        @BeforeEach
        void setUp() {
                tenantContextMock = mockStatic(TenantContext.class);
                tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

                tierId = UUID.randomUUID();
                customerId = UUID.randomUUID();
                transactionId = UUID.randomUUID();

                loyaltyTier = new LoyaltyTier();
                loyaltyTier.setId(tierId);
                loyaltyTier.setTenantId(TENANT_ID);
                loyaltyTier.setCode("GOLD");
                loyaltyTier.setName("Gold Tier");
                loyaltyTier.setMinPoints(2000);
                loyaltyTier.setDiscountPercentage(BigDecimal.TEN);
                loyaltyTier.setPointsMultiplier(BigDecimal.valueOf(2.0));
                loyaltyTier.setColor("#FFD700");
                loyaltyTier.setIcon("gold-badge");
                loyaltyTier.setBenefits("Free shipping,Priority support");
                loyaltyTier.setDisplayOrder(3);

                customer = new Customer();
                customer.setId(customerId);
                customer.setTenantId(TENANT_ID);
                customer.setCode("CUST-001");
                customer.setFirstName("John");
                customer.setLastName("Doe");
                customer.setEmail("john@example.com");
                customer.setLoyaltyTierId(tierId);
                customer.setLifetimePoints(2500);
                customer.setTotalPoints(2500);
                customer.setAvailablePoints(1000);
                customer.setIsActive(true);

                transaction = new LoyaltyTransaction();
                transaction.setId(transactionId);
                transaction.setTenantId(TENANT_ID);
                transaction.setCustomerId(customerId);
                transaction.setTransactionType(LoyaltyTransaction.LoyaltyTransactionType.EARN);
                transaction.setPoints(100);
                transaction.setBalanceAfter(1100);
                transaction.setReferenceType("ORDER");
                transaction.setReferenceId(UUID.randomUUID());
                transaction.setDescription("Points earned from purchase");

                tierRequest = LoyaltyTierRequest.builder()
                                .code("GOLD")
                                .name("Gold Tier")
                                .minPoints(2000)
                                .discountPercentage(BigDecimal.TEN)
                                .pointsMultiplier(BigDecimal.valueOf(2.0))
                                .color("#FFD700")
                                .icon("gold-badge")
                                .benefits("Free shipping,Priority support")
                                .displayOrder(3)
                                .build();

                tierResponse = LoyaltyTierResponse.builder()
                                .id(tierId)
                                .code("GOLD")
                                .name("Gold Tier")
                                .minPoints(2000)
                                .discountPercentage(BigDecimal.TEN)
                                .pointsMultiplier(BigDecimal.valueOf(2.0))
                                .color("#FFD700")
                                .icon("gold-badge")
                                .benefits("Free shipping,Priority support")
                                .displayOrder(3)
                                .build();

                transactionRequest = LoyaltyTransactionRequest.builder()
                                .customerId(customerId)
                                .pointsChange(100)
                                .transactionType(LoyaltyTransaction.LoyaltyTransactionType.EARN)
                                .description("Points earned from purchase")
                                .build();

                transactionResponse = LoyaltyTransactionResponse.builder()
                                .id(transactionId)
                                .customerId(customerId)
                                .transactionType(LoyaltyTransaction.LoyaltyTransactionType.EARN)
                                .points(100)
                                .balanceAfter(1100)
                                .referenceType("ORDER")
                                .description("Points earned from purchase")
                                .build();
        }

        @AfterEach
        void tearDown() {
                tenantContextMock.close();
        }

        @Nested
        @DisplayName("createTier tests")
        class CreateTierTests {

                @Test
                @DisplayName("Should create tier successfully")
                void shouldCreateTierSuccessfully() {
                        when(loyaltyTierRepository.existsByTenantIdAndCode(TENANT_ID, "GOLD")).thenReturn(false);
                        when(adminMapper.toLoyaltyTier(tierRequest)).thenReturn(loyaltyTier);
                        when(loyaltyTierRepository.save(loyaltyTier)).thenReturn(loyaltyTier);
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        LoyaltyTierResponse result = loyaltyService.createTier(tierRequest);

                        assertThat(result).isNotNull();
                        assertThat(result.getCode()).isEqualTo("GOLD");
                        verify(loyaltyTierRepository).save(loyaltyTier);
                }

                @Test
                @DisplayName("Should throw exception when tier code already exists")
                void shouldThrowExceptionWhenTierCodeExists() {
                        when(loyaltyTierRepository.existsByTenantIdAndCode(TENANT_ID, "GOLD")).thenReturn(true);

                        assertThatThrownBy(() -> loyaltyService.createTier(tierRequest))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("already exists");
                }

                @Test
                @DisplayName("Should throw exception when request is null")
                void shouldThrowExceptionWhenRequestIsNull() {
                        assertThatThrownBy(() -> loyaltyService.createTier(null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("getTierById tests")
        class GetTierByIdTests {

                @Test
                @DisplayName("Should return tier when found")
                void shouldReturnTierWhenFound() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        LoyaltyTierResponse result = loyaltyService.getTierById(tierId);

                        assertThat(result).isNotNull();
                        assertThat(result.getId()).isEqualTo(tierId);
                }

                @Test
                @DisplayName("Should throw exception when tier not found")
                void shouldThrowExceptionWhenNotFound() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.getTierById(tierId))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw exception when id is null")
                void shouldThrowExceptionWhenIdIsNull() {
                        assertThatThrownBy(() -> loyaltyService.getTierById(null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("getTierByCode tests")
        class GetTierByCodeTests {

                @Test
                @DisplayName("Should return tier when found by code")
                void shouldReturnTierWhenFoundByCode() {
                        when(loyaltyTierRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "GOLD"))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        LoyaltyTierResponse result = loyaltyService.getTierByCode("GOLD");

                        assertThat(result).isNotNull();
                        assertThat(result.getCode()).isEqualTo("GOLD");
                }

                @Test
                @DisplayName("Should throw exception when tier not found by code")
                void shouldThrowExceptionWhenNotFoundByCode() {
                        when(loyaltyTierRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "INVALID"))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.getTierByCode("INVALID"))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }
        }

        @Nested
        @DisplayName("getAllTiers tests")
        class GetAllTiersTests {

                @Test
                @DisplayName("Should return all tiers paginated")
                void shouldReturnAllTiersPaginated() {
                        Pageable pageable = PageRequest.of(0, 10);
                        Page<LoyaltyTier> page = new PageImpl<>(List.of(loyaltyTier));

                        when(loyaltyTierRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable))
                                        .thenReturn(page);
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        var result = loyaltyService.getAllTiers(pageable);

                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("getAllTiersOrdered tests")
        class GetAllTiersOrderedTests {

                @Test
                @DisplayName("Should return all tiers ordered by min points")
                void shouldReturnAllTiersOrderedByMinPoints() {
                        when(loyaltyTierRepository.findByTenantIdAndDeletedAtIsNullOrderByMinPointsAsc(TENANT_ID))
                                        .thenReturn(List.of(loyaltyTier));
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        List<LoyaltyTierResponse> result = loyaltyService.getAllTiersOrdered();

                        assertThat(result).hasSize(1);
                }
        }

        @Nested
        @DisplayName("updateTier tests")
        class UpdateTierTests {

                @Test
                @DisplayName("Should update tier successfully")
                void shouldUpdateTierSuccessfully() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(loyaltyTierRepository.save(loyaltyTier)).thenReturn(loyaltyTier);
                        when(adminMapper.toLoyaltyTierResponse(loyaltyTier)).thenReturn(tierResponse);

                        LoyaltyTierResponse result = loyaltyService.updateTier(tierId, tierRequest);

                        assertThat(result).isNotNull();
                        verify(adminMapper).updateLoyaltyTierFromRequest(tierRequest, loyaltyTier);
                        verify(loyaltyTierRepository).save(loyaltyTier);
                }

                @Test
                @DisplayName("Should throw exception when tier not found")
                void shouldThrowExceptionWhenTierNotFound() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.updateTier(tierId, tierRequest))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw exception when id is null")
                void shouldThrowExceptionWhenIdIsNull() {
                        assertThatThrownBy(() -> loyaltyService.updateTier(null, tierRequest))
                                        .isInstanceOf(NullPointerException.class);
                }

                @Test
                @DisplayName("Should throw exception when request is null")
                void shouldThrowExceptionWhenRequestIsNull() {
                        assertThatThrownBy(() -> loyaltyService.updateTier(tierId, null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("deleteTier tests")
        class DeleteTierTests {

                @Test
                @DisplayName("Should soft delete tier successfully")
                void shouldSoftDeleteTierSuccessfully() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(loyaltyTierRepository.save(loyaltyTier)).thenReturn(loyaltyTier);

                        loyaltyService.deleteTier(tierId);

                        verify(loyaltyTierRepository).save(loyaltyTier);
                }

                @Test
                @DisplayName("Should throw exception when tier not found")
                void shouldThrowExceptionWhenTierNotFound() {
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.deleteTier(tierId))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw exception when id is null")
                void shouldThrowExceptionWhenIdIsNull() {
                        assertThatThrownBy(() -> loyaltyService.deleteTier(null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("createTransaction tests")
        class CreateTransactionTests {

                @Test
                @DisplayName("Should create earn transaction successfully")
                void shouldCreateEarnTransactionSuccessfully() {
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));
                        when(adminMapper.toLoyaltyTransaction(transactionRequest)).thenReturn(transaction);
                        when(loyaltyTransactionRepository.save(transaction)).thenReturn(transaction);
                        when(loyaltyTierRepository.findTierForPoints(eq(TENANT_ID), anyInt()))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(customerRepository.save(customer)).thenReturn(customer);
                        when(adminMapper.toLoyaltyTransactionResponse(transaction)).thenReturn(transactionResponse);

                        LoyaltyTransactionResponse result = loyaltyService.createTransaction(transactionRequest);

                        assertThat(result).isNotNull();
                        assertThat(result.getPoints()).isEqualTo(100);
                        verify(loyaltyTransactionRepository).save(transaction);
                        verify(customerRepository).save(customer);
                }

                @Test
                @DisplayName("Should create redemption transaction successfully")
                void shouldCreateRedemptionTransactionSuccessfully() {
                        LoyaltyTransactionRequest redeemRequest = LoyaltyTransactionRequest.builder()
                                        .customerId(customerId)
                                        .pointsChange(-500)
                                        .transactionType(LoyaltyTransaction.LoyaltyTransactionType.REDEEM)
                                        .description("Points redeemed for discount")
                                        .build();
                        LoyaltyTransaction redeemTransaction = new LoyaltyTransaction();
                        redeemTransaction.setId(transactionId);
                        redeemTransaction.setTenantId(TENANT_ID);
                        redeemTransaction.setCustomerId(customerId);
                        redeemTransaction.setTransactionType(LoyaltyTransaction.LoyaltyTransactionType.REDEEM);
                        redeemTransaction.setPoints(-500);
                        redeemTransaction.setBalanceAfter(500);

                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));
                        when(adminMapper.toLoyaltyTransaction(redeemRequest)).thenReturn(redeemTransaction);
                        when(loyaltyTransactionRepository.save(redeemTransaction)).thenReturn(redeemTransaction);
                        when(loyaltyTierRepository.findTierForPoints(eq(TENANT_ID), anyInt()))
                                        .thenReturn(Optional.of(loyaltyTier));
                        when(customerRepository.save(customer)).thenReturn(customer);
                        when(adminMapper.toLoyaltyTransactionResponse(redeemTransaction))
                                        .thenReturn(transactionResponse);

                        LoyaltyTransactionResponse result = loyaltyService.createTransaction(redeemRequest);

                        assertThat(result).isNotNull();
                        verify(customerRepository).save(customer);
                }

                @Test
                @DisplayName("Should throw exception when insufficient points for redemption")
                void shouldThrowExceptionWhenInsufficientPoints() {
                        LoyaltyTransactionRequest redeemRequest = LoyaltyTransactionRequest.builder()
                                        .customerId(customerId)
                                        .pointsChange(-5000)
                                        .transactionType(LoyaltyTransaction.LoyaltyTransactionType.REDEEM)
                                        .description("Redemption test")
                                        .build();

                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));

                        assertThatThrownBy(() -> loyaltyService.createTransaction(redeemRequest))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Insufficient points");
                }

                @Test
                @DisplayName("Should throw exception when customer not found")
                void shouldThrowExceptionWhenCustomerNotFound() {
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.createTransaction(transactionRequest))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw exception when request is null")
                void shouldThrowExceptionWhenRequestIsNull() {
                        assertThatThrownBy(() -> loyaltyService.createTransaction(null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("getTransactionById tests")
        class GetTransactionByIdTests {

                @Test
                @DisplayName("Should return transaction when found")
                void shouldReturnTransactionWhenFound() {
                        when(loyaltyTransactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(transactionId,
                                        TENANT_ID))
                                        .thenReturn(Optional.of(transaction));
                        when(adminMapper.toLoyaltyTransactionResponse(transaction)).thenReturn(transactionResponse);

                        LoyaltyTransactionResponse result = loyaltyService.getTransactionById(transactionId);

                        assertThat(result).isNotNull();
                        assertThat(result.getId()).isEqualTo(transactionId);
                }

                @Test
                @DisplayName("Should throw exception when transaction not found")
                void shouldThrowExceptionWhenNotFound() {
                        when(loyaltyTransactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(transactionId,
                                        TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.getTransactionById(transactionId))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should throw exception when id is null")
                void shouldThrowExceptionWhenIdIsNull() {
                        assertThatThrownBy(() -> loyaltyService.getTransactionById(null))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("getTransactionsByCustomer tests")
        class GetTransactionsByCustomerTests {

                @Test
                @DisplayName("Should return transactions by customer")
                void shouldReturnTransactionsByCustomer() {
                        Pageable pageable = PageRequest.of(0, 10);
                        Page<LoyaltyTransaction> page = new PageImpl<>(List.of(transaction));

                        when(loyaltyTransactionRepository.findByTenantIdAndCustomerIdAndDeletedAtIsNull(
                                        TENANT_ID, customerId, pageable)).thenReturn(page);
                        when(adminMapper.toLoyaltyTransactionResponse(transaction)).thenReturn(transactionResponse);

                        var result = loyaltyService.getTransactionsByCustomer(customerId, pageable);

                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(1);
                }

                @Test
                @DisplayName("Should throw exception when customerId is null")
                void shouldThrowExceptionWhenCustomerIdIsNull() {
                        Pageable pageable = PageRequest.of(0, 10);
                        assertThatThrownBy(() -> loyaltyService.getTransactionsByCustomer(null, pageable))
                                        .isInstanceOf(NullPointerException.class);
                }
        }

        @Nested
        @DisplayName("getAllTransactions tests")
        class GetAllTransactionsTests {

                @Test
                @DisplayName("Should return all transactions paginated")
                void shouldReturnAllTransactionsPaginated() {
                        Pageable pageable = PageRequest.of(0, 10);
                        Page<LoyaltyTransaction> page = new PageImpl<>(List.of(transaction));

                        when(loyaltyTransactionRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable))
                                        .thenReturn(page);
                        when(adminMapper.toLoyaltyTransactionResponse(transaction)).thenReturn(transactionResponse);

                        var result = loyaltyService.getAllTransactions(pageable);

                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("calculatePointsForPurchase tests")
        class CalculatePointsForPurchaseTests {

                @Test
                @DisplayName("Should calculate points with tier multiplier")
                void shouldCalculatePointsWithTierMultiplier() {
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.of(loyaltyTier));

                        int result = loyaltyService.calculatePointsForPurchase(customerId,
                                        BigDecimal.valueOf(100), BigDecimal.ONE);

                        // 100 * 1 * 2.0 = 200 points
                        assertThat(result).isEqualTo(200);
                }

                @Test
                @DisplayName("Should calculate points without tier multiplier")
                void shouldCalculatePointsWithoutTierMultiplier() {
                        customer.setLoyaltyTierId(null);
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));

                        int result = loyaltyService.calculatePointsForPurchase(customerId,
                                        BigDecimal.valueOf(100), BigDecimal.ONE);

                        // 100 * 1 * 1 = 100 points
                        assertThat(result).isEqualTo(100);
                }

                @Test
                @DisplayName("Should calculate points when tier not found")
                void shouldCalculatePointsWhenTierNotFound() {
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.of(customer));
                        when(loyaltyTierRepository.findByIdAndTenantIdAndDeletedAtIsNull(tierId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        int result = loyaltyService.calculatePointsForPurchase(customerId,
                                        BigDecimal.valueOf(100), BigDecimal.ONE);

                        // 100 * 1 * 1 = 100 points (default multiplier)
                        assertThat(result).isEqualTo(100);
                }

                @Test
                @DisplayName("Should throw exception when customer not found")
                void shouldThrowExceptionWhenCustomerNotFound() {
                        when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> loyaltyService.calculatePointsForPurchase(customerId,
                                        BigDecimal.valueOf(100), BigDecimal.ONE))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }
        }
}
