package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Store entity.
 * Stores are physical locations belonging to branches.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Optional<Store> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Page<Store> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Store> findByTenantIdAndIsActiveAndDeletedAtIsNull(String tenantId, Boolean isActive, Pageable pageable);

    Page<Store> findByTenantIdAndBranchIdAndDeletedAtIsNull(String tenantId, UUID branchId, Pageable pageable);

    List<Store> findByTenantIdAndBranchIdAndIsActiveAndDeletedAtIsNull(String tenantId, UUID branchId,
            Boolean isActive);

    boolean existsByTenantIdAndCode(String tenantId, String code);

    long countByTenantIdAndDeletedAtIsNull(String tenantId);

    long countByTenantIdAndBranchIdAndDeletedAtIsNull(String tenantId, UUID branchId);
}
