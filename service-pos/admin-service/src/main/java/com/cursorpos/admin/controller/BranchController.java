package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.service.BranchService;
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
 * REST controller for branch management.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAuthority('BRANCH_CREATE')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Branch created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_READ')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable UUID id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('BRANCH_READ')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchByCode(@PathVariable String code) {
        BranchResponse response = branchService.getBranchByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BRANCH_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> getAllBranches(Pageable pageable) {
        PagedResponse<BranchResponse> response = branchService.getAllBranches(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAuthority('BRANCH_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<BranchResponse>>> getBranchesByStore(
            @PathVariable UUID storeId,
            Pageable pageable) {
        PagedResponse<BranchResponse> response = branchService.getBranchesByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/active")
    @PreAuthorize("hasAuthority('BRANCH_READ')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getActiveBranchesByStore(@PathVariable UUID storeId) {
        List<BranchResponse> response = branchService.getActiveBranchesByStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_UPDATE')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BRANCH_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }
}
