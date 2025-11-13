package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.service.SettingsService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for settings management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS_WRITE')")
    public ResponseEntity<ApiResponse<SettingsResponse>> createOrUpdateSetting(@Valid @RequestBody SettingsRequest request) {
        SettingsResponse response = settingsService.createOrUpdateSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Setting saved successfully"));
    }

    @GetMapping("/key/{settingKey}")
    @PreAuthorize("hasAuthority('SETTINGS_READ')")
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettingByKey(@PathVariable String settingKey) {
        SettingsResponse response = settingsService.getSettingByKey(settingKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('SETTINGS_READ')")
    public ResponseEntity<ApiResponse<List<SettingsResponse>>> getSettingsByCategory(@PathVariable String category) {
        List<SettingsResponse> response = settingsService.getSettingsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<SettingsResponse>>> getAllSettings(Pageable pageable) {
        PagedResponse<SettingsResponse> response = settingsService.getAllSettings(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_WRITE')")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable UUID id) {
        settingsService.deleteSetting(id);
        return ResponseEntity.ok(ApiResponse.success("Setting deleted successfully"));
    }
}
