package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.dto.LoyaltyTierRequest;
import com.cursorpos.admin.dto.LoyaltyTierResponse;
import com.cursorpos.admin.dto.LoyaltyTransactionRequest;
import com.cursorpos.admin.dto.LoyaltyTransactionResponse;
import com.cursorpos.admin.entity.LoyaltyTransaction.LoyaltyTransactionType;
import com.cursorpos.shared.dto.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LoyaltyController.
 * Uses real database with test security configuration.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(IntegrationTestSecurityConfig.class)
@DisplayName("LoyaltyController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class LoyaltyControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-loyalty-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "LOYALTY_TIER_CREATE,LOYALTY_TIER_READ,LOYALTY_TIER_UPDATE,LOYALTY_TIER_DELETE,LOYALTY_TRANSACTION_CREATE,LOYALTY_TRANSACTION_READ,CUSTOMER_CREATE";
        private static final String BASE_URL = "/loyalty";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private LoyaltyTierRequest tierRequest;

        @BeforeEach
        void setUp() {
                tierRequest = LoyaltyTierRequest.builder()
                                .code("TIER-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Tier")
                                .minPoints(0)
                                .discountPercentage(new BigDecimal("5.00"))
                                .pointsMultiplier(new BigDecimal("1.0"))
                                .displayOrder(1)
                                .build();
        }

        @Nested
        @DisplayName("Loyalty Tier Tests")
        class LoyaltyTierTests {

                @Test
                @DisplayName("Should create loyalty tier with valid request")
                void shouldCreateTierWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.code").value(tierRequest.getCode()))
                                        .andExpect(jsonPath("$.data.name").value("Test Tier"))
                                        .andReturn();

                        ApiResponse<LoyaltyTierResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTierResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                }

                @Test
                @DisplayName("Should return 400 for invalid tier request - missing code")
                void shouldReturn400ForInvalidTierRequest() throws Exception {
                        LoyaltyTierRequest invalidRequest = LoyaltyTierRequest.builder()
                                        .name("Tier Name")
                                        .minPoints(0)
                                        .build();

                        mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalidRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return tier by id")
                void shouldReturnTierById() throws Exception {
                        // Create a tier
                        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<LoyaltyTierResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTierResponse>>() {
                                        });
                        UUID tierId = createResponse.getData().getId();

                        // Retrieve by id
                        mockMvc.perform(get(BASE_URL + "/tiers/{id}", tierId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(tierId.toString()));
                }

                @Test
                @DisplayName("Should return 404 when tier not found")
                void shouldReturn404WhenTierNotFound() throws Exception {
                        mockMvc.perform(get(BASE_URL + "/tiers/{id}", UUID.randomUUID())
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return tier by code")
                void shouldReturnTierByCode() throws Exception {
                        // Create a tier
                        mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated());

                        // Retrieve by code
                        mockMvc.perform(get(BASE_URL + "/tiers/code/{code}", tierRequest.getCode())
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.code").value(tierRequest.getCode()));
                }

                @Test
                @DisplayName("Should return all tiers paginated")
                void shouldReturnAllTiersPaginated() throws Exception {
                        // Create a tier
                        mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated());

                        // Get all tiers
                        mockMvc.perform(get(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }

                @Test
                @DisplayName("Should return all tiers ordered")
                void shouldReturnAllTiersOrdered() throws Exception {
                        // Create a tier
                        mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated());

                        // Get all tiers ordered
                        mockMvc.perform(get(BASE_URL + "/tiers/ordered")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isArray());
                }

                @Test
                @DisplayName("Should update tier successfully")
                void shouldUpdateTierSuccessfully() throws Exception {
                        // Create a tier
                        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<LoyaltyTierResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTierResponse>>() {
                                        });
                        UUID tierId = createResponse.getData().getId();

                        // Update it
                        LoyaltyTierRequest updateRequest = LoyaltyTierRequest.builder()
                                        .code(tierRequest.getCode())
                                        .name("Updated Tier Name")
                                        .minPoints(0)
                                        .discountPercentage(new BigDecimal("10.00"))
                                        .pointsMultiplier(new BigDecimal("1.5"))
                                        .build();

                        mockMvc.perform(put(BASE_URL + "/tiers/{id}", tierId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.name").value("Updated Tier Name"));
                }

                @Test
                @DisplayName("Should delete tier successfully")
                void shouldDeleteTierSuccessfully() throws Exception {
                        // Create a tier
                        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/tiers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(tierRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<LoyaltyTierResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTierResponse>>() {
                                        });
                        UUID tierId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + "/tiers/{id}", tierId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));

                        // Verify deleted
                        mockMvc.perform(get(BASE_URL + "/tiers/{id}", tierId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("Loyalty Transaction Tests")
        class LoyaltyTransactionTests {

                private UUID testCustomerId;

                @BeforeEach
                void setUp() throws Exception {
                        // Create a customer for transactions
                        CreateCustomerRequest customerRequest = CreateCustomerRequest.builder()
                                        .code("CUST-" + UUID.randomUUID().toString().substring(0, 8))
                                        .firstName("Test")
                                        .lastName("Customer")
                                        .email("customer@test.com")
                                        .build();

                        MvcResult customerResult = mockMvc.perform(post("/customers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(customerRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> customerResponse = objectMapper.readValue(
                                        customerResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        testCustomerId = customerResponse.getData().getId();
                }

                @Test
                @DisplayName("Should create loyalty transaction with valid request")
                void shouldCreateTransactionWithValidRequest() throws Exception {
                        LoyaltyTransactionRequest transactionRequest = LoyaltyTransactionRequest.builder()
                                        .customerId(testCustomerId)
                                        .transactionType(LoyaltyTransactionType.EARN)
                                        .pointsChange(100)
                                        .description("Test transaction")
                                        .build();

                        MvcResult result = mockMvc.perform(post(BASE_URL + "/transactions")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(transactionRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.points").value(100))
                                        .andReturn();

                        ApiResponse<LoyaltyTransactionResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTransactionResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                }

                @Test
                @DisplayName("Should return transaction by id")
                void shouldReturnTransactionById() throws Exception {
                        // Create a transaction
                        LoyaltyTransactionRequest transactionRequest = LoyaltyTransactionRequest.builder()
                                        .customerId(testCustomerId)
                                        .transactionType(LoyaltyTransactionType.EARN)
                                        .pointsChange(50)
                                        .description("Test transaction")
                                        .build();

                        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/transactions")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(transactionRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<LoyaltyTransactionResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<LoyaltyTransactionResponse>>() {
                                        });
                        UUID transactionId = createResponse.getData().getId();

                        // Retrieve by id
                        mockMvc.perform(get(BASE_URL + "/transactions/{id}", transactionId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(transactionId.toString()));
                }

                @Test
                @DisplayName("Should return transactions by customer")
                void shouldReturnTransactionsByCustomer() throws Exception {
                        // Create a transaction
                        LoyaltyTransactionRequest transactionRequest = LoyaltyTransactionRequest.builder()
                                        .customerId(testCustomerId)
                                        .transactionType(LoyaltyTransactionType.EARN)
                                        .pointsChange(50)
                                        .description("Test transaction by customer")
                                        .build();

                        mockMvc.perform(post(BASE_URL + "/transactions")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(transactionRequest)))
                                        .andExpect(status().isCreated());

                        // Get transactions by customer
                        mockMvc.perform(get(BASE_URL + "/transactions/customer/{customerId}", testCustomerId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }

                @Test
                @DisplayName("Should return all transactions")
                void shouldReturnAllTransactions() throws Exception {
                        mockMvc.perform(get(BASE_URL + "/transactions")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }
        }

        @Nested
        @DisplayName("Utility Endpoint Tests")
        class UtilityEndpointTests {

                @Test
                @DisplayName("Should calculate points for purchase")
                void shouldCalculatePointsForPurchase() throws Exception {
                        // Create a customer first
                        CreateCustomerRequest customerRequest = CreateCustomerRequest.builder()
                                        .code("CUST-" + UUID.randomUUID().toString().substring(0, 8))
                                        .firstName("Test")
                                        .lastName("Customer")
                                        .email("customer@test.com")
                                        .build();

                        MvcResult customerResult = mockMvc.perform(post("/customers")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(customerRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> customerResponse = objectMapper.readValue(
                                        customerResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        UUID customerId = customerResponse.getData().getId();

                        // Calculate points
                        mockMvc.perform(get(BASE_URL + "/calculate-points")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .param("customerId", customerId.toString())
                                        .param("purchaseAmount", "100.00")
                                        .param("loyaltyPointsPerCurrency", "1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isNumber());
                }
        }
}
