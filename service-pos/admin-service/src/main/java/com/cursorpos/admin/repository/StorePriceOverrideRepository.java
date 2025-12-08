package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.StorePriceOverride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for StorePriceOverride entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Repository
public interface StorePriceOverrideRepository extends JpaRepository<StorePriceOverride, UUID> {

        Optional<StorePriceOverride> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

        Page<StorePriceOverride> findByTenantIdAndStoreIdAndDeletedAtIsNull(String tenantId, UUID storeId,
                        Pageable pageable);

        Page<StorePriceOverride> findByTenantIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID productId,
                        Pageable pageable);

        Optional<StorePriceOverride> findByTenantIdAndStoreIdAndProductIdAndDeletedAtIsNull(
                        String tenantId, UUID storeId, UUID productId);

        /**
         * Find active price override for a store and product.
         */
        @Query("SELECT spo FROM StorePriceOverride spo WHERE spo.tenantId = :tenantId " +
                        "AND spo.storeId = :storeId AND spo.productId = :productId " +
                        "AND spo.isActive = true AND spo.deletedAt IS NULL " +
                        "AND (spo.effectiveFrom IS NULL OR spo.effectiveFrom <= :now) " +
                        "AND (spo.effectiveTo IS NULL OR spo.effectiveTo >= :now)")
        Optional<StorePriceOverride> findActiveOverride(
                        @Param("tenantId") String tenantId,
                        @Param("storeId") UUID storeId,
                        @Param("productId") UUID productId,
                        @Param("now") Instant now);

        /**
         * Find all active price overrides for a store.
         */
        @Query("SELECT spo FROM StorePriceOverride spo WHERE spo.tenantId = :tenantId " +
                        "AND spo.storeId = :storeId AND spo.isActive = true AND spo.deletedAt IS NULL " +
                        "AND (spo.effectiveFrom IS NULL OR spo.effectiveFrom <= :now) " +
                        "AND (spo.effectiveTo IS NULL OR spo.effectiveTo >= :now)")
        List<StorePriceOverride> findAllActiveOverridesForStore(
                        @Param("tenantId") String tenantId,
                        @Param("storeId") UUID storeId,
                        @Param("now") Instant now);

        /**
         * Check if an override exists for a store and product combination.
         */
        boolean existsByTenantIdAndStoreIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID storeId, UUID productId);
}
