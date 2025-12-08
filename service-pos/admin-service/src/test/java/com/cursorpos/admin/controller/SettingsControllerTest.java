package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.service.SettingsService;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.GlobalExceptionHandler;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for SettingsController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SettingsController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SettingsControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private SettingsService settingsService;

    private SettingsController settingsController;

    private UUID settingsId;
    private SettingsRequest request;
    private SettingsResponse response;

    @BeforeEach
    void setUp() {
        // Create controller with mocked service
        settingsController = new SettingsController(settingsService);

        // Setup standalone MockMvc with the controller, exception handler, and pageable
        // support
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        settingsId = UUID.randomUUID();

        request = SettingsRequest.builder()
                .settingKey("store.tax.rate")
                .settingValue("0.10")
                .category("TAX")
                .valueType("NUMBER")
                .description("Default tax rate")
                .isEncrypted(false)
                .build();

        response = SettingsResponse.builder()
                .id(settingsId)
                .settingKey("store.tax.rate")
                .settingValue("0.10")
                .category("TAX")
                .valueType("NUMBER")
                .description("Default tax rate")
                .isSystem(false)
                .isEncrypted(false)
                .build();
    }

    @Nested
    @DisplayName("POST /settings")
    class CreateOrUpdateSettingTests {

        @Test
        @DisplayName("Should create setting with valid request")
        void shouldCreateSettingWithValidRequest() throws Exception {
            when(settingsService.createOrUpdateSetting(any(SettingsRequest.class))).thenReturn(response);

            mockMvc.perform(post("/settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.settingKey").value("store.tax.rate"));
        }
    }

    @Nested
    @DisplayName("GET /settings/key/{settingKey}")
    class GetSettingByKeyTests {

        @Test
        @DisplayName("Should return setting when found")
        void shouldReturnSettingWhenFound() throws Exception {
            when(settingsService.getSettingByKey("store.tax.rate")).thenReturn(response);

            mockMvc.perform(get("/settings/key/{settingKey}", "store.tax.rate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.settingKey").value("store.tax.rate"));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(settingsService.getSettingByKey("unknown.key"))
                    .thenThrow(new ResourceNotFoundException("Setting not found"));

            mockMvc.perform(get("/settings/key/{settingKey}", "unknown.key"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /settings/category/{category}")
    class GetSettingsByCategoryTests {

        @Test
        @DisplayName("Should return settings by category")
        void shouldReturnSettingsByCategory() throws Exception {
            when(settingsService.getSettingsByCategory("TAX")).thenReturn(List.of(response));

            mockMvc.perform(get("/settings/category/{category}", "TAX"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].category").value("TAX"));
        }

        @Test
        @DisplayName("Should return empty list when no settings in category")
        void shouldReturnEmptyListWhenNone() throws Exception {
            when(settingsService.getSettingsByCategory("UNKNOWN")).thenReturn(List.of());

            mockMvc.perform(get("/settings/category/{category}", "UNKNOWN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /settings")
    class GetAllSettingsTests {

        @Test
        @DisplayName("Should return all settings with pagination")
        void shouldReturnAllSettingsWithPagination() throws Exception {
            SettingsResponse anotherResponse = SettingsResponse.builder()
                    .id(UUID.randomUUID())
                    .settingKey("store.currency")
                    .settingValue("USD")
                    .category("GENERAL")
                    .valueType("STRING")
                    .description("Default currency")
                    .isSystem(false)
                    .isEncrypted(false)
                    .build();
            PagedResponse<SettingsResponse> pagedResponse = PagedResponse.<SettingsResponse>builder()
                    .content(List.of(response, anotherResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(2)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .numberOfElements(2)
                    .build();
            when(settingsService.getAllSettings(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/settings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /settings/{id}")
    class DeleteSettingTests {

        @Test
        @DisplayName("Should delete setting successfully")
        void shouldDeleteSettingSuccessfully() throws Exception {
            doNothing().when(settingsService).deleteSetting(settingsId);

            mockMvc.perform(delete("/settings/{id}", settingsId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent setting")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Setting not found")).when(settingsService).deleteSetting(settingsId);

            mockMvc.perform(delete("/settings/{id}", settingsId))
                    .andExpect(status().isNotFound());
        }
    }
}
