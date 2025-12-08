package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.TenantResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TenantController.
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
@DisplayName("TenantController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class TenantControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-admin-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "TENANT_CREATE,TENANT_READ,TENANT_UPDATE,TENANT_DELETE";
        private static final String BASE_URL = "/tenants";

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

        private CreateTenantRequest createRequest;

        @BeforeEach
        void setUp() {
                createRequest = CreateTenantRequest.builder()
                                .code("tenant-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Tenant")
                                .subdomain("test-" + UUID.randomUUID().toString().substring(0, 8))
                                .businessType("RETAIL")
                                .email("tenant@test.com")
                                .phone("555-0001")
                                .address("123 Tenant St")
                                .city("Test City")
                                .state("TS")
                                .country("USA")
                                .postalCode("12345")
                                .build();
        }

        @Nested
        @DisplayName("POST /tenants")
        class CreateTenantTests {

                @Test
                @DisplayName("Should create tenant with valid request")
                void shouldCreateTenantWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()))
                                        .andExpect(jsonPath("$.data.name").value("Test Tenant"))
                                        .andReturn();

                        ApiResponse<TenantResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                        assertThat(response.getData().getIsActive()).isTrue();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing code")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        CreateTenantRequest invalidRequest = CreateTenantRequest.builder()
                                        .name("Tenant Name")
                                        .email("tenant@test.com")
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
        @DisplayName("GET /tenants/{id}")
        class GetTenantByIdTests {

                @Test
                @DisplayName("Should return tenant when found")
                void shouldReturnTenantWhenFound() throws Exception {
                        // First create a tenant
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<TenantResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });
                        UUID tenantId = createResponse.getData().getId();

                        // Then retrieve it
                        mockMvc.perform(get(BASE_URL + PATH_ID, tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.id").value(tenantId.toString()));
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
        @DisplayName("GET /tenants/code/{code}")
        class GetTenantByCodeTests {

                @Test
                @DisplayName("Should return tenant when found by code")
                void shouldReturnTenantWhenFoundByCode() throws Exception {
                        // First create a tenant
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
                        mockMvc.perform(get(BASE_URL + "/code/{code}", "non-existent-code")
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /tenants")
        class GetAllTenantsTests {

                @Test
                @DisplayName("Should return paginated tenants")
                void shouldReturnPaginatedTenants() throws Exception {
                        // Create a tenant first
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all tenants
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
        @DisplayName("PUT /tenants/{id}")
        class UpdateTenantTests {

                @Test
                @DisplayName("Should update tenant successfully")
                void shouldUpdateTenantSuccessfully() throws Exception {
                        // Create a tenant
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<TenantResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });
                        UUID tenantId = createResponse.getData().getId();

                        // Update it
                        CreateTenantRequest updateRequest = CreateTenantRequest.builder()
                                        .code(createRequest.getCode())
                                        .name("Updated Tenant Name")
                                        .email("updated@test.com")
                                        .build();

                        mockMvc.perform(put(BASE_URL + PATH_ID, tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.name").value("Updated Tenant Name"));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent tenant")
                void shouldReturn404WhenUpdatingNonExistentTenant() throws Exception {
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
        @DisplayName("DELETE /tenants/{id}")
        class DeleteTenantTests {

                @Test
                @DisplayName("Should delete tenant successfully")
                void shouldDeleteTenantSuccessfully() throws Exception {
                        // Create a tenant
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<TenantResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });
                        UUID tenantId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + PATH_ID, tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true));

                        // Verify it's deleted (soft delete - should return 404)
                        mockMvc.perform(get(BASE_URL + PATH_ID, tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent tenant")
                void shouldReturn404WhenDeletingNonExistentTenant() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /tenants/{id}/activate")
        class ActivateTenantTests {

                @Test
                @DisplayName("Should activate tenant successfully")
                void shouldActivateTenantSuccessfully() throws Exception {
                        // Create and then deactivate a tenant
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<TenantResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });
                        UUID tenantId = createResponse.getData().getId();

                        // Deactivate first
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk());

                        // Then activate
                        mockMvc.perform(post(BASE_URL + "/{id}/activate", tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(true));
                }
        }

        @Nested
        @DisplayName("POST /tenants/{id}/deactivate")
        class DeactivateTenantTests {

                @Test
                @DisplayName("Should deactivate tenant successfully")
                void shouldDeactivateTenantSuccessfully() throws Exception {
                        // Create a tenant
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<TenantResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<TenantResponse>>() {
                                        });
                        UUID tenantId = createResponse.getData().getId();

                        // Deactivate it
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", tenantId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(false));
                }
        }
}
