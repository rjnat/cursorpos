package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
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
 * Integration tests for CustomerController.
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
@DisplayName("CustomerController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class CustomerControllerIntegrationTest {

        // Test configuration constants
        private static final String TEST_TENANT = "tenant-customer-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "CUSTOMER_CREATE,CUSTOMER_READ,CUSTOMER_UPDATE,CUSTOMER_DELETE";
        private static final String BASE_URL = "/customers";

        // HTTP header constants
        private static final String HEADER_TENANT_ID = "X-Tenant-ID";
        private static final String HEADER_USER_ID = "X-User-ID";
        private static final String HEADER_PERMISSIONS = "X-Permissions";

        // JSON path constants
        private static final String JSON_SUCCESS = "$.success";
        private static final String PATH_ID = "/{id}";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private CreateCustomerRequest createRequest;

        @BeforeEach
        void setUp() {
                createRequest = CreateCustomerRequest.builder()
                                .code("CUST-" + UUID.randomUUID().toString().substring(0, 8))
                                .firstName("Test")
                                .lastName("Customer")
                                .email("customer@test.com")
                                .phone("555-0001")
                                .address("123 Customer St")
                                .city("Test City")
                                .state("TS")
                                .country("USA")
                                .postalCode("12345")
                                .build();
        }

        @Nested
        @DisplayName("POST /customers")
        class CreateCustomerTests {

                @Test
                @DisplayName("Should create customer with valid request")
                void shouldCreateCustomerWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()))
                                        .andExpect(jsonPath("$.data.firstName").value("Test"))
                                        .andReturn();

                        ApiResponse<CustomerResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing code")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        CreateCustomerRequest invalidRequest = CreateCustomerRequest.builder()
                                        .firstName("Test")
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
        @DisplayName("GET /customers/{id}")
        class GetCustomerByIdTests {

                @Test
                @DisplayName("Should return customer when found")
                void shouldReturnCustomerWhenFound() throws Exception {
                        // First create a customer
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        UUID customerId = createResponse.getData().getId();

                        // Then retrieve it
                        mockMvc.perform(get(BASE_URL + PATH_ID, customerId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.id").value(customerId.toString()));
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
        @DisplayName("GET /customers/code/{code}")
        class GetCustomerByCodeTests {

                @Test
                @DisplayName("Should return customer when found by code")
                void shouldReturnCustomerWhenFoundByCode() throws Exception {
                        // First create a customer
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
        @DisplayName("GET /customers")
        class GetAllCustomersTests {

                @Test
                @DisplayName("Should return paginated customers")
                void shouldReturnPaginatedCustomers() throws Exception {
                        // Create a customer first
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all customers
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
        @DisplayName("PUT /customers/{id}")
        class UpdateCustomerTests {

                @Test
                @DisplayName("Should update customer successfully")
                void shouldUpdateCustomerSuccessfully() throws Exception {
                        // Create a customer
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        UUID customerId = createResponse.getData().getId();

                        // Update it
                        CreateCustomerRequest updateRequest = CreateCustomerRequest.builder()
                                        .code(createRequest.getCode())
                                        .firstName("Updated")
                                        .lastName("Name")
                                        .email("updated@test.com")
                                        .build();

                        mockMvc.perform(put(BASE_URL + PATH_ID, customerId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.firstName").value("Updated"));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent customer")
                void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
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
        @DisplayName("DELETE /customers/{id}")
        class DeleteCustomerTests {

                @Test
                @DisplayName("Should delete customer successfully")
                void shouldDeleteCustomerSuccessfully() throws Exception {
                        // Create a customer
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        UUID customerId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + PATH_ID, customerId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true));

                        // Verify it's deleted (soft delete - should return 404)
                        mockMvc.perform(get(BASE_URL + PATH_ID, customerId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent customer")
                void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /customers/{id}/loyalty-points")
        class AddLoyaltyPointsTests {

                @Test
                @DisplayName("Should add loyalty points successfully")
                void shouldAddLoyaltyPointsSuccessfully() throws Exception {
                        // Create a customer
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<CustomerResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<CustomerResponse>>() {
                                        });
                        UUID customerId = createResponse.getData().getId();

                        // Add loyalty points
                        mockMvc.perform(post(BASE_URL + PATH_ID + "/loyalty-points", customerId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .param("points", "100"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.totalPoints").value(100));
                }
        }
}
