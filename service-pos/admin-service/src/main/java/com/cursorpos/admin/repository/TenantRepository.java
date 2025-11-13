package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tenant entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findBySubdomain(String subdomain);

    Optional<Tenant> findByIdAndDeletedAtIsNull(UUID id);

    Page<Tenant> findByDeletedAtIsNull(Pageable pageable);

    Page<Tenant> findByIsActiveAndDeletedAtIsNull(Boolean isActive, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsBySubdomain(String subdomain);
}
