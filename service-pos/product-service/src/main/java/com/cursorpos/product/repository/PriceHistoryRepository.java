package com.cursorpos.product.repository;

import com.cursorpos.product.entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PriceHistory entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {

    Page<PriceHistory> findByTenantIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID productId, Pageable pageable);

    List<PriceHistory> findByTenantIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID productId);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.tenantId = :tenantId AND ph.product.id = :productId " +
            "AND ph.deletedAt IS NULL AND ph.effectiveFrom <= :date " +
            "AND (ph.effectiveTo IS NULL OR ph.effectiveTo > :date) " +
            "ORDER BY ph.effectiveFrom DESC")
    Optional<PriceHistory> findEffectivePrice(@Param("tenantId") String tenantId,
            @Param("productId") UUID productId,
            @Param("date") LocalDateTime date);
}
