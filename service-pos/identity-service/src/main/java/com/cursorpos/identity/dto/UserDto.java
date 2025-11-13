package com.cursorpos.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * User DTO for API responses.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String tenantId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Boolean emailVerified;
    private Instant lastLoginAt;
    private List<String> roles;
    private List<String> permissions;
    private Instant createdAt;
    private Instant updatedAt;
}
