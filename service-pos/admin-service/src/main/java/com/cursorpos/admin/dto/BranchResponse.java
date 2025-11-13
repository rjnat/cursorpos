package com.cursorpos.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for branch response.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private UUID id;
    private UUID storeId;
    private String code;
    private String name;
    private String description;
    private String branchType;
    private Boolean isActive;
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    private Instant createdAt;
    private Instant updatedAt;
}
