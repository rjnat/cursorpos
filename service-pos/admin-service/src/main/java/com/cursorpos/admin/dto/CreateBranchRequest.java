package com.cursorpos.admin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new branch.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Branch type must not exceed 50 characters")
    private String branchType;

    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    private String managerName;

    @Email(message = "Manager email must be valid")
    @Size(max = 255, message = "Manager email must not exceed 255 characters")
    private String managerEmail;

    @Size(max = 20, message = "Manager phone must not exceed 20 characters")
    private String managerPhone;
}
