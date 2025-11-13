package com.cursorpos.product.repository;

import com.cursorpos.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Optional<Category> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Page<Category> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    List<Category> findByTenantIdAndParentIdAndDeletedAtIsNull(String tenantId, UUID parentId);

    List<Category> findByTenantIdAndIsActiveAndDeletedAtIsNull(String tenantId, Boolean isActive);

    boolean existsByTenantIdAndCode(String tenantId, String code);
}
