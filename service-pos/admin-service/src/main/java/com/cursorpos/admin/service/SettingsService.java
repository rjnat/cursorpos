package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.entity.Settings;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.SettingsRepository;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing settings.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public SettingsResponse createOrUpdateSetting(SettingsRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating or updating setting with key: {} for tenant: {}", request.getSettingKey(), tenantId);

        Settings settings = settingsRepository
                .findByTenantIdAndSettingKeyAndDeletedAtIsNull(tenantId, request.getSettingKey())
                .orElse(null);

        if (settings == null) {
            settings = adminMapper.toSettings(request);
            settings.setTenantId(tenantId);
        } else {
            if (Boolean.TRUE.equals(settings.getIsSystem())) {
                throw new IllegalArgumentException("Cannot modify system settings");
            }
            adminMapper.updateSettingsFromRequest(request, settings);
        }

        Settings saved = settingsRepository.save(settings);
        log.info("Setting saved successfully with ID: {}", saved.getId());
        return adminMapper.toSettingsResponse(saved);
    }

    @Transactional(readOnly = true)
    public SettingsResponse getSettingByKey(String settingKey) {
        String tenantId = TenantContext.getTenantId();
        Settings settings = settingsRepository.findByTenantIdAndSettingKeyAndDeletedAtIsNull(tenantId, settingKey)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + settingKey));
        return adminMapper.toSettingsResponse(settings);
    }

    @Transactional(readOnly = true)
    public List<SettingsResponse> getSettingsByCategory(String category) {
        String tenantId = TenantContext.getTenantId();
        List<Settings> settings = settingsRepository.findByTenantIdAndCategoryAndDeletedAtIsNull(tenantId, category);
        return settings.stream()
                .map(adminMapper::toSettingsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SettingsResponse> getAllSettings(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Settings> page = settingsRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(adminMapper::toSettingsResponse));
    }

    @Transactional
    public void deleteSetting(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting setting with ID: {} for tenant: {}", id, tenantId);

        Settings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with ID: " + id));

        if (!settings.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Setting not found with ID: " + id);
        }

        if (Boolean.TRUE.equals(settings.getIsSystem())) {
            throw new IllegalArgumentException("Cannot delete system settings");
        }

        settings.softDelete();
        settingsRepository.save(settings);

        log.info("Setting soft-deleted successfully with ID: {}", id);
    }
}
