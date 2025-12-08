package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.entity.Settings;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.SettingsRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SettingsService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private SettingsService settingsService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = "tenant-test-001";
    private UUID settingsId;
    private Settings settings;
    private SettingsRequest request;
    private SettingsResponse response;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

        settingsId = UUID.randomUUID();

        settings = new Settings();
        settings.setId(settingsId);
        settings.setTenantId(TENANT_ID);
        settings.setSettingKey("pos.tax.rate");
        settings.setSettingValue("0.10");
        settings.setCategory("POS");
        settings.setDescription("Default tax rate");
        settings.setIsSystem(false);

        request = SettingsRequest.builder()
                .settingKey("pos.tax.rate")
                .settingValue("0.10")
                .category("POS")
                .valueType("NUMBER")
                .description("Default tax rate")
                .isEncrypted(false)
                .build();

        response = SettingsResponse.builder()
                .id(settingsId)
                .settingKey("pos.tax.rate")
                .settingValue("0.10")
                .category("POS")
                .description("Default tax rate")
                .isSystem(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("createOrUpdateSetting tests")
    class CreateOrUpdateSettingTests {

        @Test
        @DisplayName("Should create new setting when not exists")
        void shouldCreateNewSettingWhenNotExists() {
            when(settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(TENANT_ID, "pos.tax.rate"))
                    .thenReturn(Optional.empty());
            when(adminMapper.toSettings(request)).thenReturn(settings);
            when(settingsRepository.save(settings)).thenReturn(settings);
            when(adminMapper.toSettingsResponse(settings)).thenReturn(response);

            SettingsResponse result = settingsService.createOrUpdateSetting(request);

            assertThat(result).isNotNull();
            assertThat(result.getSettingKey()).isEqualTo("pos.tax.rate");
            verify(settingsRepository).save(settings);
        }

        @Test
        @DisplayName("Should update existing setting")
        void shouldUpdateExistingSetting() {
            when(settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(TENANT_ID, "pos.tax.rate"))
                    .thenReturn(Optional.of(settings));
            when(settingsRepository.save(settings)).thenReturn(settings);
            when(adminMapper.toSettingsResponse(settings)).thenReturn(response);

            SettingsResponse result = settingsService.createOrUpdateSetting(request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateSettingsFromRequest(request, settings);
            verify(settingsRepository).save(settings);
        }

        @Test
        @DisplayName("Should throw exception when trying to update system setting")
        void shouldThrowExceptionWhenTryingToUpdateSystemSetting() {
            settings.setIsSystem(true);
            when(settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(TENANT_ID, "pos.tax.rate"))
                    .thenReturn(Optional.of(settings));

            assertThatThrownBy(() -> settingsService.createOrUpdateSetting(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("system settings");
        }
    }

    @Nested
    @DisplayName("getSettingByKey tests")
    class GetSettingByKeyTests {

        @Test
        @DisplayName("Should return setting when found by key")
        void shouldReturnSettingWhenFoundByKey() {
            when(settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(TENANT_ID, "pos.tax.rate"))
                    .thenReturn(Optional.of(settings));
            when(adminMapper.toSettingsResponse(settings)).thenReturn(response);

            SettingsResponse result = settingsService.getSettingByKey("pos.tax.rate");

            assertThat(result).isNotNull();
            assertThat(result.getSettingKey()).isEqualTo("pos.tax.rate");
        }

        @Test
        @DisplayName("Should throw exception when setting not found by key")
        void shouldThrowExceptionWhenNotFoundByKey() {
            when(settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(TENANT_ID, "invalid.key"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> settingsService.getSettingByKey("invalid.key"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getSettingsByCategory tests")
    class GetSettingsByCategoryTests {

        @Test
        @DisplayName("Should return settings by category")
        void shouldReturnSettingsByCategory() {
            when(settingsRepository.findByTenantIdAndCategoryAndDeletedAtIsNull(TENANT_ID, "POS"))
                    .thenReturn(List.of(settings));
            when(adminMapper.toSettingsResponse(settings)).thenReturn(response);

            List<SettingsResponse> result = settingsService.getSettingsByCategory("POS");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("POS");
        }
    }

    @Nested
    @DisplayName("getAllSettings tests")
    class GetAllSettingsTests {

        @Test
        @DisplayName("Should return all settings paginated")
        void shouldReturnAllSettingsPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Settings> page = new PageImpl<>(List.of(settings));

            when(settingsRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable)).thenReturn(page);
            when(adminMapper.toSettingsResponse(settings)).thenReturn(response);

            var result = settingsService.getAllSettings(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteSetting tests")
    class DeleteSettingTests {

        @Test
        @DisplayName("Should soft delete setting successfully")
        void shouldSoftDeleteSettingSuccessfully() {
            when(settingsRepository.findById(settingsId)).thenReturn(Optional.of(settings));
            when(settingsRepository.save(settings)).thenReturn(settings);

            settingsService.deleteSetting(settingsId);

            verify(settingsRepository).save(settings);
        }

        @Test
        @DisplayName("Should throw exception when setting not found")
        void shouldThrowExceptionWhenSettingNotFound() {
            when(settingsRepository.findById(settingsId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settingsService.deleteSetting(settingsId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when setting belongs to different tenant")
        void shouldThrowExceptionWhenSettingBelongsToDifferentTenant() {
            settings.setTenantId("different-tenant");
            when(settingsRepository.findById(settingsId)).thenReturn(Optional.of(settings));

            assertThatThrownBy(() -> settingsService.deleteSetting(settingsId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when trying to delete system setting")
        void shouldThrowExceptionWhenTryingToDeleteSystemSetting() {
            settings.setIsSystem(true);
            when(settingsRepository.findById(settingsId)).thenReturn(Optional.of(settings));

            assertThatThrownBy(() -> settingsService.deleteSetting(settingsId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("system settings");
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> settingsService.deleteSetting(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
