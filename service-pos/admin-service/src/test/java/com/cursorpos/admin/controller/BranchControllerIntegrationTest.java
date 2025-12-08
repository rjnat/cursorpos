package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
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
 * Integration tests for BranchController.
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
@DisplayName("BranchController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class BranchControllerIntegrationTest {

        // Test configuration constants
        private static final String TEST_TENANT = "tenant-branch-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "BRANCH_CREATE,BRANCH_READ,BRANCH_UPDATE,BRANCH_DELETE";
        private static final String BASE_URL = "/branches";

        // HTTP header constants
        private static final String HEADER_TENANT_ID = "X-Tenant-ID";
        private static final String HEADER_USER_ID = "X-User-ID";
        private static final String HEADER_PERMISSIONS = "X-Permissions";

        // JSON path constants
        private static final String JSON_SUCCESS = "$.success";
        private static final String JSON_DATA_ID = "$.data.id";
        private static final String PATH_ID = "/{id}";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private CreateBranchRequest createRequest;

        @BeforeEach
        void setUp() {
                createRequest = CreateBranchRequest.builder()
                                .code("BR-" + UUID.randomUUID().toString().substring(0, 8))
                                .name("Test Branch")
                                .description("Test branch for integration tests")
                                .address("123 Test St")
                                .city("Test City")
                                .state("TS")
                                .country("USA")
                                .postalCode("12345")
                                .phone("555-0001")
                                .email("test@branch.com")
                                .managerName("Test Manager")
                                .managerEmail("manager@branch.com")
                                .managerPhone("555-0002")
                                .build();
        }

        @Nested
        @DisplayName("POST /branches")
        class CreateBranchTests {

                @Test
                @DisplayName("Should create branch with valid request")
                void shouldCreateBranchWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.code").value(createRequest.getCode()))
                                        .andExpect(jsonPath("$.data.name").value("Test Branch"))
                                        .andReturn();

                        ApiResponse<BranchResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                        assertThat(response.getData().getIsActive()).isTrue();
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing code")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        CreateBranchRequest invalidRequest = CreateBranchRequest.builder()
                                        .name("Branch Name")
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
        @DisplayName("GET /branches/{id}")
        class GetBranchByIdTests {

                @Test
                @DisplayName("Should return branch when found")
                void shouldReturnBranchWhenFound() throws Exception {
                        // First create a branch
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<BranchResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });
                        UUID branchId = createResponse.getData().getId();

                        // Then retrieve it
                        mockMvc.perform(get(BASE_URL + PATH_ID, branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath(JSON_DATA_ID).value(branchId.toString()));
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
        @DisplayName("GET /branches/code/{code}")
        class GetBranchByCodeTests {

                @Test
                @DisplayName("Should return branch when found by code")
                void shouldReturnBranchWhenFoundByCode() throws Exception {
                        // First create a branch
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
        @DisplayName("GET /branches")
        class GetAllBranchesTests {

                @Test
                @DisplayName("Should return paginated branches")
                void shouldReturnPaginatedBranches() throws Exception {
                        // Create a branch first
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all branches
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
        @DisplayName("GET /branches/active")
        class GetActiveBranchesTests {

                @Test
                @DisplayName("Should return active branches")
                void shouldReturnActiveBranches() throws Exception {
                        // Create a branch (active by default)
                        mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get active branches
                        mockMvc.perform(get(BASE_URL + "/active")
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data").isArray());
                }
        }

        @Nested
        @DisplayName("PUT /branches/{id}")
        class UpdateBranchTests {

                @Test
                @DisplayName("Should update branch successfully")
                void shouldUpdateBranchSuccessfully() throws Exception {
                        // Create a branch
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<BranchResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });
                        UUID branchId = createResponse.getData().getId();

                        // Update it
                        CreateBranchRequest updateRequest = CreateBranchRequest.builder()
                                        .code(createRequest.getCode())
                                        .name("Updated Branch Name")
                                        .description("Updated description")
                                        .address("456 Updated St")
                                        .city("Updated City")
                                        .state("UC")
                                        .country("USA")
                                        .postalCode("54321")
                                        .phone("555-9999")
                                        .email("updated@branch.com")
                                        .build();

                        mockMvc.perform(put(BASE_URL + PATH_ID, branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.name").value("Updated Branch Name"));
                }

                @Test
                @DisplayName("Should return 404 when updating non-existent branch")
                void shouldReturn404WhenUpdatingNonExistentBranch() throws Exception {
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
        @DisplayName("DELETE /branches/{id}")
        class DeleteBranchTests {

                @Test
                @DisplayName("Should delete branch successfully")
                void shouldDeleteBranchSuccessfully() throws Exception {
                        // Create a branch
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<BranchResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });
                        UUID branchId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + PATH_ID, branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true));

                        // Verify it's deleted (soft delete - should return 404)
                        mockMvc.perform(get(BASE_URL + PATH_ID, branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent branch")
                void shouldReturn404WhenDeletingNonExistentBranch() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + PATH_ID, nonExistentId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /branches/{id}/activate")
        class ActivateBranchTests {

                @Test
                @DisplayName("Should activate branch successfully")
                void shouldActivateBranchSuccessfully() throws Exception {
                        // Create and then deactivate a branch
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<BranchResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });
                        UUID branchId = createResponse.getData().getId();

                        // Deactivate first
                        mockMvc.perform(post(BASE_URL + PATH_ID + "/deactivate", branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk());

                        // Then activate
                        mockMvc.perform(post(BASE_URL + PATH_ID + "/activate", branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(true));
                }
        }

        @Nested
        @DisplayName("POST /branches/{id}/deactivate")
        class DeactivateBranchTests {

                @Test
                @DisplayName("Should deactivate branch successfully")
                void shouldDeactivateBranchSuccessfully() throws Exception {
                        // Create a branch
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<BranchResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<BranchResponse>>() {
                                        });
                        UUID branchId = createResponse.getData().getId();

                        // Deactivate it
                        mockMvc.perform(post(BASE_URL + PATH_ID + "/deactivate", branchId)
                                        .header(HEADER_TENANT_ID, TEST_TENANT)
                                        .header(HEADER_USER_ID, TEST_USER)
                                        .header(HEADER_PERMISSIONS, ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(JSON_SUCCESS).value(true))
                                        .andExpect(jsonPath("$.data.isActive").value(false));
                }
        }
}
