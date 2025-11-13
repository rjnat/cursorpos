package com.cursorpos.admin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a setting.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsRequest {

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotBlank(message = "Setting key is required")
    @Size(max = 100, message = "Setting key must not exceed 100 characters")
    private String settingKey;

    private String settingValue;

    @NotBlank(message = "Value type is required")
    @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON|ARRAY)$", message = "Value type must be STRING, NUMBER, BOOLEAN, JSON, or ARRAY")
    private String valueType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean isEncrypted;
}
