package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Branch entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Optional<Branch> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Page<Branch> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Branch> findByTenantIdAndStoreIdAndDeletedAtIsNull(String tenantId, UUID storeId, Pageable pageable);

    List<Branch> findByTenantIdAndStoreIdAndIsActiveAndDeletedAtIsNull(String tenantId, UUID storeId, Boolean isActive);

    boolean existsByTenantIdAndCode(String tenantId, String code);
}
