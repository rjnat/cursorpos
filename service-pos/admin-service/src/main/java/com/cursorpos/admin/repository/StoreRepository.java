package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Store entity.
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

    boolean existsByTenantIdAndCode(String tenantId, String code);
}
