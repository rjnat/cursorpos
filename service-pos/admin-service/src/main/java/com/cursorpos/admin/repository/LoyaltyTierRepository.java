package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.LoyaltyTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LoyaltyTier entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, UUID> {

    Optional<LoyaltyTier> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Optional<LoyaltyTier> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Page<LoyaltyTier> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    List<LoyaltyTier> findByTenantIdAndIsActiveAndDeletedAtIsNull(String tenantId, Boolean isActive);

    List<LoyaltyTier> findByTenantIdAndDeletedAtIsNullOrderByMinPointsAsc(String tenantId);

    boolean existsByTenantIdAndCode(String tenantId, String code);

    /**
     * Find the appropriate tier for a given point total.
     * Returns the highest tier where minPoints <= totalPoints.
     */
    @Query("SELECT lt FROM LoyaltyTier lt WHERE lt.tenantId = :tenantId " +
            "AND lt.minPoints <= :totalPoints AND lt.isActive = true AND lt.deletedAt IS NULL " +
            "ORDER BY lt.minPoints DESC LIMIT 1")
    Optional<LoyaltyTier> findTierForPoints(@Param("tenantId") String tenantId,
            @Param("totalPoints") Integer totalPoints);
}
