package com.cursorpos.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Role DTO for API responses.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
    private Boolean isActive;
    private Instant createdAt;
}
