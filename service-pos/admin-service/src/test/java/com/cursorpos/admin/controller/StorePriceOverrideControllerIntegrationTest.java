package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.dto.StoreResponse;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StorePriceOverrideController.
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
@DisplayName("StorePriceOverrideController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StorePriceOverrideControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-price-override-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "PRICE_OVERRIDE_CREATE,PRICE_OVERRIDE_READ,PRICE_OVERRIDE_UPDATE,PRICE_OVERRIDE_DELETE,BRANCH_CREATE,STORE_CREATE";
        private static final String BASE_URL = "/price-overrides";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private UUID testStoreId;
        private UUID testProductId;
        private StorePriceOverrideRequest createRequest;

        @BeforeEach
        void setUp() throws Exception {
                // Create a branch first
                CreateBranchRequest branchRequest = CreateBranchRequest.builder()
                                .code("BR-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Branch")
                                .address("123 Test St")
                                .city("Test City")
                                .country("USA")
                                .build();

                MvcResult branchResult = mockMvc.perform(post("/branches")
                                .header("X-Tenant-ID", TEST_TENANT)
                                .header("X-User-ID", TEST_USER)
                                .header("X-Permissions", ALL_PERMISSIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(branchRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                ApiResponse<BranchResponse> branchResponse = objectMapper.readValue(
                                branchResult.getResponse().getContentAsString(),
                                new TypeReference<ApiResponse<BranchResponse>>() {
                                });
                UUID branchId = branchResponse.getData().getId();

                // Create a store
                CreateStoreRequest storeRequest = CreateStoreRequest.builder()
                                .branchId(branchId)
                                .code("ST-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Store")
                                .address("123 Store St")
                                .city("Test City")
                                .country("USA")
                                .build();

                MvcResult storeResult = mockMvc.perform(post("/stores")
                                .header("X-Tenant-ID", TEST_TENANT)
                                .header("X-User-ID", TEST_USER)
                                .header("X-Permissions", ALL_PERMISSIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(storeRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                ApiResponse<StoreResponse> storeResponse = objectMapper.readValue(
                                storeResult.getResponse().getContentAsString(),
                                new TypeReference<ApiResponse<StoreResponse>>() {
                                });
                testStoreId = storeResponse.getData().getId();

                // Use a mock product ID
                testProductId = UUID.randomUUID();

                createRequest = StorePriceOverrideRequest.builder()
                                .storeId(testStoreId)
                                .productId(testProductId)
                                .overridePrice(new BigDecimal("19.99"))
                                .discountPercentage(new BigDecimal("10.00"))
                                .effectiveFrom(Instant.now())
                                .effectiveTo(Instant.now().plus(30, ChronoUnit.DAYS))
                                .build();
        }

        @Nested
        @DisplayName("POST /price-overrides")
        class CreateOverrideTests {

                @Test
                @DisplayName("Should create price override with valid request")
                void shouldCreateOverrideWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.storeId").value(testStoreId.toString()))
                                        .andExpect(jsonPath("$.data.productId").value(testProductId.toString()))
                                        .andReturn();

                        ApiResponse<StorePriceOverrideResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StorePriceOverrideResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing store ID")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        StorePriceOverrideRequest invalidRequest = StorePriceOverrideRequest.builder()
                                        .productId(testProductId)
                                        .overridePrice(new BigDecimal("9.99"))
                                        .build();

                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalidRequest)))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("GET /price-overrides/{id}")
        class GetOverrideByIdTests {

                @Test
                @DisplayName("Should return override when found")
                void shouldReturnOverrideWhenFound() throws Exception {
                        // Create an override
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StorePriceOverrideResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StorePriceOverrideResponse>>() {
                                        });
                        UUID overrideId = createResponse.getData().getId();

                        // Retrieve by id
                        mockMvc.perform(get(BASE_URL + "/{id}", overrideId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(overrideId.toString()));
                }

                @Test
                @DisplayName("Should return 404 when not found")
                void shouldReturn404WhenNotFound() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /price-overrides/store/{storeId}")
        class GetOverridesByStoreTests {

                @Test
                @DisplayName("Should return overrides by store")
                void shouldReturnOverridesByStore() throws Exception {
                        // Create an override
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get overrides by store
                        mockMvc.perform(get(BASE_URL + "/store/{storeId}", testStoreId)
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
        @DisplayName("GET /price-overrides/product/{productId}")
        class GetOverridesByProductTests {

                @Test
                @DisplayName("Should return overrides by product")
                void shouldReturnOverridesByProduct() throws Exception {
                        // Create an override
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get overrides by product
                        mockMvc.perform(get(BASE_URL + "/product/{productId}", testProductId)
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
        @DisplayName("GET /price-overrides/active")
        class GetActiveOverrideTests {

                @Test
                @DisplayName("Should return active override when exists")
                void shouldReturnActiveOverrideWhenExists() throws Exception {
                        // Create an override with current effective dates
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get active override
                        mockMvc.perform(get(BASE_URL + "/active")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .param("storeId", testStoreId.toString())
                                        .param("productId", testProductId.toString()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }
        }

        @Nested
        @DisplayName("PUT /price-overrides/{id}")
        class UpdateOverrideTests {

                @Test
                @DisplayName("Should update override successfully")
                void shouldUpdateOverrideSuccessfully() throws Exception {
                        // Create an override
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StorePriceOverrideResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StorePriceOverrideResponse>>() {
                                        });
                        UUID overrideId = createResponse.getData().getId();

                        // Update it
                        StorePriceOverrideRequest updateRequest = StorePriceOverrideRequest.builder()
                                        .storeId(testStoreId)
                                        .productId(testProductId)
                                        .overridePrice(new BigDecimal("24.99"))
                                        .discountPercentage(new BigDecimal("15.00"))
                                        .build();

                        mockMvc.perform(put(BASE_URL + "/{id}", overrideId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.overridePrice").value(24.99));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent override")
                void shouldReturn404WhenUpdatingNonExistentOverride() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(put(BASE_URL + "/{id}", nonExistentId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("DELETE /price-overrides/{id}")
        class DeleteOverrideTests {

                @Test
                @DisplayName("Should delete override successfully")
                void shouldDeleteOverrideSuccessfully() throws Exception {
                        // Create an override
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StorePriceOverrideResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StorePriceOverrideResponse>>() {
                                        });
                        UUID overrideId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + "/{id}", overrideId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));

                        // Verify deleted
                        mockMvc.perform(get(BASE_URL + "/{id}", overrideId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent override")
                void shouldReturn404WhenDeletingNonExistentOverride() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + "/{id}", nonExistentId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }
}
