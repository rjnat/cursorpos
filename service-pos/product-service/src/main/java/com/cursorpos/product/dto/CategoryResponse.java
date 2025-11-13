package com.cursorpos.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for categories.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private UUID id;
    private String tenantId;
    private String code;
    private String name;
    private String description;
    private UUID parentId;
    private String parentName;
    private Boolean isActive;
    private Integer displayOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
