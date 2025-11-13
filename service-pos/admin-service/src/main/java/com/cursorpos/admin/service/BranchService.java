package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.entity.Branch;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.BranchRepository;
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
import java.util.Objects;

/**
 * Service for managing branches.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private static final String BRANCH_NOT_FOUND_MSG = "Branch not found with ID: ";

    private final BranchRepository branchRepository;
    private final AdminMapper adminMapper;

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating branch with code: {} for tenant: {}", request.getCode(), tenantId);

        if (branchRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException("Branch with code " + request.getCode() + " already exists");
        }

        Branch branch = adminMapper.toBranch(request);
        branch.setTenantId(tenantId);
        Branch saved = branchRepository.save(branch);

        log.info("Branch created successfully with ID: {}", saved.getId());
        return adminMapper.toBranchResponse(saved);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Objects.requireNonNull(id, "id");
        Branch branch = branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(BRANCH_NOT_FOUND_MSG + id));
        return adminMapper.toBranchResponse(branch);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchByCode(String code) {
        String tenantId = TenantContext.getTenantId();
        Branch branch = branchRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with code: " + code));
        return adminMapper.toBranchResponse(branch);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BranchResponse> getAllBranches(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Branch> page = branchRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PagedResponse.of(page.map(adminMapper::toBranchResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BranchResponse> getBranchesByStore(UUID storeId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Branch> page = branchRepository.findByTenantIdAndStoreIdAndDeletedAtIsNull(tenantId, storeId, pageable);
        return PagedResponse.of(page.map(adminMapper::toBranchResponse));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getActiveBranchesByStore(UUID storeId) {
        String tenantId = TenantContext.getTenantId();
        List<Branch> branches = branchRepository.findByTenantIdAndStoreIdAndIsActiveAndDeletedAtIsNull(tenantId,
                storeId, true);
        return branches.stream()
                .map(adminMapper::toBranchResponse)
                .toList();
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, CreateBranchRequest request) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating branch with ID: {} for tenant: {}", id, tenantId);

        Branch branch = branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(BRANCH_NOT_FOUND_MSG + id));

        adminMapper.updateBranchFromRequest(request, branch);
        Branch updated = branchRepository.save(branch);

        log.info("Branch updated successfully with ID: {}", updated.getId());
        return adminMapper.toBranchResponse(updated);
    }

    @Transactional
    public void deleteBranch(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deleting branch with ID: {} for tenant: {}", id, tenantId);

        Branch branch = branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(BRANCH_NOT_FOUND_MSG + id));

        branch.softDelete();
        branchRepository.save(branch);

        log.info("Branch soft-deleted successfully with ID: {}", id);
    }
}
