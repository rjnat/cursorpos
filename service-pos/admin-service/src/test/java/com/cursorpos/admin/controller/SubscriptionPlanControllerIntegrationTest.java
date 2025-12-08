package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
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
 * Integration tests for SubscriptionPlanController.
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
@DisplayName("SubscriptionPlanController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SubscriptionPlanControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-subscription-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "SUBSCRIPTION_PLAN_CREATE,SUBSCRIPTION_PLAN_READ,SUBSCRIPTION_PLAN_UPDATE,SUBSCRIPTION_PLAN_DELETE";
        private static final String BASE_URL = "/subscription-plans";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private SubscriptionPlanRequest createRequest;

        @BeforeEach
        void setUp() {
                createRequest = SubscriptionPlanRequest.builder()
                                .code("PLAN-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Plan")
                                .description("Test subscription plan")
                                .priceMonthly(new BigDecimal("29.99"))
                                .priceYearly(new BigDecimal("299.99"))
                                .maxUsers(10)
                                .maxStores(5)
                                .maxProducts(1000)
                                .build();
        }

        @Nested
        @DisplayName("POST /subscription-plans")
        class CreatePlanTests {

                @Test
                @DisplayName("Should create plan with valid request")
                void shouldCreatePlanWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()))
                                        .andExpect(jsonPath("$.data.name").value("Test Plan"))
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                        assertThat(response.getData().getIsActive()).isTrue();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing code")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        SubscriptionPlanRequest invalidRequest = SubscriptionPlanRequest.builder()
                                        .name("Plan Name")
                                        .priceMonthly(new BigDecimal("9.99"))
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
        @DisplayName("GET /subscription-plans/{id}")
        class GetPlanByIdTests {

                @Test
                @DisplayName("Should return plan when found")
                void shouldReturnPlanWhenFound() throws Exception {
                        // Create a plan
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });
                        UUID planId = createResponse.getData().getId();

                        // Retrieve by id
                        mockMvc.perform(get(BASE_URL + "/{id}", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.id").value(planId.toString()));
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
        @DisplayName("GET /subscription-plans/code/{planCode}")
        class GetPlanByCodeTests {

                @Test
                @DisplayName("Should return plan when found by code")
                void shouldReturnPlanWhenFoundByCode() throws Exception {
                        // Create a plan
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Retrieve by code
                        mockMvc.perform(get(BASE_URL + "/code/{planCode}", createRequest.getCode())
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()));
                }

                @Test
                @DisplayName("Should return 404 when not found by code")
                void shouldReturn404WhenNotFoundByCode() throws Exception {
                        mockMvc.perform(get(BASE_URL + "/code/{planCode}", "NON-EXISTENT-CODE")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /subscription-plans")
        class GetAllPlansTests {

                @Test
                @DisplayName("Should return paginated plans")
                void shouldReturnPaginatedPlans() throws Exception {
                        // Create a plan
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all plans
                        mockMvc.perform(get(BASE_URL)
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
        @DisplayName("GET /subscription-plans/active")
        class GetActivePlansTests {

                @Test
                @DisplayName("Should return active plans")
                void shouldReturnActivePlans() throws Exception {
                        // Create a plan (active by default)
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get active plans (no permission required)
                        mockMvc.perform(get(BASE_URL + "/active")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isArray());
                }
        }

        @Nested
        @DisplayName("PUT /subscription-plans/{id}")
        class UpdatePlanTests {

                @Test
                @DisplayName("Should update plan successfully")
                void shouldUpdatePlanSuccessfully() throws Exception {
                        // Create a plan
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });
                        UUID planId = createResponse.getData().getId();

                        // Update it
                        SubscriptionPlanRequest updateRequest = SubscriptionPlanRequest.builder()
                                        .code(createRequest.getCode())
                                        .name("Updated Plan Name")
                                        .priceMonthly(new BigDecimal("39.99"))
                                        .priceYearly(new BigDecimal("399.99"))
                                        .maxUsers(20)
                                        .maxStores(5)
                                        .maxProducts(1000)
                                        .build();

                        mockMvc.perform(put(BASE_URL + "/{id}", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.name").value("Updated Plan Name"));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent plan")
                void shouldReturn404WhenUpdatingNonExistentPlan() throws Exception {
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
        @DisplayName("DELETE /subscription-plans/{id}")
        class DeletePlanTests {

                @Test
                @DisplayName("Should delete plan successfully")
                void shouldDeletePlanSuccessfully() throws Exception {
                        // Create a plan
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });
                        UUID planId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + "/{id}", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));

                        // Verify deleted
                        mockMvc.perform(get(BASE_URL + "/{id}", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent plan")
                void shouldReturn404WhenDeletingNonExistentPlan() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + "/{id}", nonExistentId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /subscription-plans/{id}/activate")
        class ActivatePlanTests {

                @Test
                @DisplayName("Should activate plan successfully")
                void shouldActivatePlanSuccessfully() throws Exception {
                        // Create a plan
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });
                        UUID planId = createResponse.getData().getId();

                        // Deactivate first
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk());

                        // Then activate
                        mockMvc.perform(post(BASE_URL + "/{id}/activate", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(true));
                }
        }

        @Nested
        @DisplayName("POST /subscription-plans/{id}/deactivate")
        class DeactivatePlanTests {

                @Test
                @DisplayName("Should deactivate plan successfully")
                void shouldDeactivatePlanSuccessfully() throws Exception {
                        // Create a plan
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SubscriptionPlanResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SubscriptionPlanResponse>>() {
                                        });
                        UUID planId = createResponse.getData().getId();

                        // Deactivate it
                        mockMvc.perform(post(BASE_URL + "/{id}/deactivate", planId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(false));
                }
        }
}
