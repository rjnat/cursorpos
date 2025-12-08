package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.dto.CreateStoreRequest;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StoreController.
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
@DisplayName("StoreController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StoreControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-store-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "STORE_CREATE,STORE_READ,STORE_UPDATE,STORE_DELETE,BRANCH_CREATE";
        private static final String BASE_URL = "/stores";

        // HTTP header constants
        private static final String HEADER_TENANT_ID = "X-Tenant-ID";
        private static final String HEADER_USER_ID = "X-User-ID";
        private static final String HEADER_PERMISSIONS = "X-Permissions";
        private static final String JSON_SUCCESS = "$.success";
        private static final String PATH_ID = "/{id}";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private UUID testBranchId;
        private CreateStoreRequest createRequest;

        @BeforeEach
        void setUp() throws Exception {
                // Create a branch first since stores require a branch
                CreateBranchRequest branchRequest = CreateBranchRequest.builder()
                                .code("BR-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Branch for Store")
                                .address("123 Test St")
                                .city("Test City")
                                .country("USA")
                                .build();

                MvcResult branchResult = mockMvc.perform(post("/branches")
                                .header(HEADER_TENANT_ID, TEST_TENANT)
                                .header(HEADER_USER_ID, TEST_USER)
                                .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(branchRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                ApiResponse<BranchResponse> branchResponse = objectMapper.readValue(
                                branchResult.getResponse().getContentAsString(),
                                new TypeReference<ApiResponse<BranchResponse>>() {
                                });
                testBranchId = branchResponse.getData().getId();

                createRequest = CreateStoreRequest.builder()
                                .branchId(testBranchId)
                                .code("ST-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Store")
                                .description("Test store for integration tests")
                                .storeType("RETAIL")
                                .email("store@test.com")
                                .phone("555-0001")
                                .address("123 Store St")
                                .city("Test City")
                                .state("TS")
                                .country("USA")
                                .postalCode("12345")
                                .operatingHours("9AM-9PM")
                                .taxRate(new BigDecimal("0.08"))
                                .build();
        }

        @Nested
        @DisplayName("POST /stores")
        class CreateStoreTests {

                @Test
                @DisplayName("Should create store with valid request")
                void shouldCreateStoreWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()))
                                        .andExpect(jsonPath("$.data.name").value("Test Store"))
                                        .andReturn();

                        ApiResponse<StoreResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                        assertThat(response.getData().getBranchId()).isEqualTo(testBranchId);
                        assertThat(response.getData().getIsActive()).isTrue();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing code")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        CreateStoreRequest invalidRequest = CreateStoreRequest.builder()
                                        .branchId(testBranchId)
                                        .name("Store Name")
                                        .address("123 Test St")
                                        .city("Test City")
                                        .build();

                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalidRequest)))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("GET /stores/{id}")
        class GetStoreByIdTests {

                @Test
                @DisplayName("Should return store when found")
                void shouldReturnStoreWhenFound() throws Exception {
                        // First create a store
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StoreResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });
                        UUID storeId = createResponse.getData().getId();

                        // Then retrieve it
                        mockMvc.perform(get(BASE_URL + PATH_ID, storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.id").value(storeId.toString()));
                }

                @Test
                @DisplayName("Should return 404 when not found")
                void shouldReturn404WhenNotFound() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(get(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /stores/code/{code}")
        class GetStoreByCodeTests {

                @Test
                @DisplayName("Should return store when found by code")
                void shouldReturnStoreWhenFoundByCode() throws Exception {
                        // First create a store
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Then retrieve by code
                        mockMvc.perform(get(BASE_URL + "/code/{code}", createRequest.getCode())
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()));
                }

                @Test
                @DisplayName("Should return 404 when not found by code")
                void shouldReturn404WhenNotFoundByCode() throws Exception {
                        mockMvc.perform(get(BASE_URL + "/code/{code}", "NON-EXISTENT-CODE")
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /stores")
        class GetAllStoresTests {

                @Test
                @DisplayName("Should return paginated stores")
                void shouldReturnPaginatedStores() throws Exception {
                        // Create a store first
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all stores
                        mockMvc.perform(get(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }
        }

        @Nested
        @DisplayName("GET /stores/branch/{branchId}")
        class GetStoresByBranchTests {

                @Test
                @DisplayName("Should return stores for branch")
                void shouldReturnStoresForBranch() throws Exception {
                        // Create a store first
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get stores by branch
                        mockMvc.perform(get(BASE_URL + "/branch/{branchId}", testBranchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }
        }

        @Nested
        @DisplayName("GET /stores/branch/{branchId}/active")
        class GetActiveStoresByBranchTests {

                @Test
                @DisplayName("Should return active stores for branch")
                void shouldReturnActiveStoresForBranch() throws Exception {
                        // Create a store (active by default)
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get active stores by branch
                        mockMvc.perform(get(BASE_URL + "/branch/{branchId}/active", testBranchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data").isArray());
                }
        }

        @Nested
        @DisplayName("PUT /stores/{id}")
        class UpdateStoreTests {

                @Test
                @DisplayName("Should update store successfully")
                void shouldUpdateStoreSuccessfully() throws Exception {
                        // Create a store
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StoreResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });
                        UUID storeId = createResponse.getData().getId();

                        // Update it
                        CreateStoreRequest updateRequest = CreateStoreRequest.builder()
                                        .branchId(testBranchId)
                                        .code(createRequest.getCode())
                                        .name("Updated Store Name")
                                        .description("Updated description")
                                        .address("456 Updated St")
                                        .city("Updated City")
                                        .state("UC")
                                        .country("USA")
                                        .postalCode("54321")
                                        .build();

                        mockMvc.perform(put(BASE_URL + PATH_ID, storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.name").value("Updated Store Name"));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent store")
                void shouldReturn404WhenUpdatingNonExistentStore() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(put(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("DELETE /stores/{id}")
        class DeleteStoreTests {

                @Test
                @DisplayName("Should delete store successfully")
                void shouldDeleteStoreSuccessfully() throws Exception {
                        // Create a store
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StoreResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });
                        UUID storeId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + PATH_ID, storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true));

                        // Verify it's deleted (soft delete - should return 404)
                        mockMvc.perform(get(BASE_URL + PATH_ID, storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent store")
                void shouldReturn404WhenDeletingNonExistentStore() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /stores/{id}/activate")
        class ActivateStoreTests {

                @Test
                @DisplayName("Should activate store successfully")
                void shouldActivateStoreSuccessfully() throws Exception {
                        // Create and then deactivate a store
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StoreResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });
                        UUID storeId = createResponse.getData().getId();

                        // Deactivate first
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk());

                        // Then activate
                        mockMvc.perform(post(BASE_URL + "/{id}/activate", storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(true));
                }
        }

        @Nested
        @DisplayName("POST /stores/{id}/deactivate")
        class DeactivateStoreTests {

                @Test
                @DisplayName("Should deactivate store successfully")
                void shouldDeactivateStoreSuccessfully() throws Exception {
                        // Create a store
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<StoreResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<StoreResponse>>() {
                                        });
                        UUID storeId = createResponse.getData().getId();

                        // Deactivate it
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", storeId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(false));
                }
        }
}
