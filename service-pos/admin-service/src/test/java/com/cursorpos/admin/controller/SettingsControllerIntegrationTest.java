package com.cursorpos.admin.controller;

import com.cursorpos.admin.config.IntegrationTestSecurityConfig;
import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
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
 * Integration tests for SettingsController.
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
@DisplayName("SettingsController Integration Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SettingsControllerIntegrationTest {

        private static final String TEST_TENANT = "tenant-settings-test-001";
        private static final String TEST_USER = "test-user-001";
        private static final String ALL_PERMISSIONS = "SETTINGS_READ,SETTINGS_WRITE";
        private static final String BASE_URL = "/settings";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private SettingsRequest createRequest;

        @BeforeEach
        void setUp() {
                createRequest = SettingsRequest.builder()
                                .settingKey("TEST_KEY_" + UUID.randomUUID().toString().substring(0, 8))
                                .settingValue("test-value")
                                .category("TEST")
                                .valueType("STRING")
                                .description("Test setting")
                                .build();
        }

        @Nested
        @DisplayName("POST /settings")
        class CreateOrUpdateSettingTests {

                @Test
                @DisplayName("Should create setting with valid request")
                void shouldCreateSettingWithValidRequest() throws Exception {
                        MvcResult result = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.settingKey").value(createRequest.getSettingKey()))
                                        .andExpect(jsonPath("$.data.settingValue").value("test-value"))
                                        .andReturn();

                        ApiResponse<SettingsResponse> response = objectMapper.readValue(
                                        result.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SettingsResponse>>() {
                                        });

                        assertThat(response.getData().getId()).isNotNull();
                }

                @Test
                @DisplayName("Should update setting when key exists")
                void shouldUpdateSettingWhenKeyExists() throws Exception {
                        // Create a setting
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Update it with same key
                        SettingsRequest updateRequest = SettingsRequest.builder()
                                        .settingKey(createRequest.getSettingKey())
                                        .settingValue("updated-value")
                                        .category("TEST")
                                        .valueType("STRING")
                                        .build();

                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.settingValue").value("updated-value"));
                }

                @Test
                @DisplayName("Should return 400 for invalid request - missing key")
                void shouldReturn400ForInvalidRequest() throws Exception {
                        SettingsRequest invalidRequest = SettingsRequest.builder()
                                        .settingValue("test-value")
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
        @DisplayName("GET /settings/key/{settingKey}")
        class GetSettingByKeyTests {

                @Test
                @DisplayName("Should return setting when found by key")
                void shouldReturnSettingWhenFoundByKey() throws Exception {
                        // Create a setting
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Retrieve by key
                        mockMvc.perform(get(BASE_URL + "/key/{settingKey}", createRequest.getSettingKey())
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.settingKey").value(createRequest.getSettingKey()));
                }

                @Test
                @DisplayName("Should return 404 when not found by key")
                void shouldReturn404WhenNotFoundByKey() throws Exception {
                        mockMvc.perform(get(BASE_URL + "/key/{settingKey}", "NON_EXISTENT_KEY")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /settings/category/{category}")
        class GetSettingsByCategoryTests {

                @Test
                @DisplayName("Should return settings by category")
                void shouldReturnSettingsByCategory() throws Exception {
                        // Create a setting
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get settings by category
                        mockMvc.perform(get(BASE_URL + "/category/{category}", "TEST")
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").isArray());
                }
        }

        @Nested
        @DisplayName("GET /settings")
        class GetAllSettingsTests {

                @Test
                @DisplayName("Should return paginated settings")
                void shouldReturnPaginatedSettings() throws Exception {
                        // Create a setting
                        mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated());

                        // Get all settings
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
        @DisplayName("DELETE /settings/{id}")
        class DeleteSettingTests {

                @Test
                @DisplayName("Should delete setting successfully")
                void shouldDeleteSettingSuccessfully() throws Exception {
                        // Create a setting
                        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createRequest)))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        ApiResponse<SettingsResponse> createResponse = objectMapper.readValue(
                                        createResult.getResponse().getContentAsString(),
                                        new TypeReference<ApiResponse<SettingsResponse>>() {
                                        });
                        UUID settingId = createResponse.getData().getId();

                        // Delete it
                        mockMvc.perform(delete(BASE_URL + "/{id}", settingId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));

                        // Verify deleted
                        mockMvc.perform(get(BASE_URL + "/key/{settingKey}", createRequest.getSettingKey())
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 404 when deleting non-existent setting")
                void shouldReturn404WhenDeletingNonExistentSetting() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(delete(BASE_URL + "/{id}", nonExistentId)
                                        .header("X-Tenant-ID", TEST_TENANT)
                                        .header("X-User-ID", TEST_USER)
                                        .header("X-Permissions", ALL_PERMISSIONS))
                                        .andExpect(status().isNotFound());
                }
        }
}
