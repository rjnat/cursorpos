package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for settings response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {

    private UUID id;
    private String category;
    private String settingKey;
    private String settingValue;
    private String valueType;
    private String description;
    private Boolean isSystem;
    private Boolean isEncrypted;
    private Instant createdAt;
    private Instant updatedAt;
}
