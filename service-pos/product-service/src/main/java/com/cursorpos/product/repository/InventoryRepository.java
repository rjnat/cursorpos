package com.cursorpos.product.repository;

import com.cursorpos.product.entity.Inventory;
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
 * Repository for Inventory entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Optional<Inventory> findByTenantIdAndProductIdAndBranchIdAndDeletedAtIsNull(String tenantId, UUID productId, UUID branchId);

    Page<Inventory> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Inventory> findByTenantIdAndBranchIdAndDeletedAtIsNull(String tenantId, UUID branchId, Pageable pageable);

    List<Inventory> findByTenantIdAndProductIdAndDeletedAtIsNull(String tenantId, UUID productId);

    @Query("SELECT i FROM Inventory i WHERE i.tenantId = :tenantId AND i.deletedAt IS NULL " +
           "AND i.quantityAvailable < i.reorderPoint")
    List<Inventory> findLowStockItems(@Param("tenantId") String tenantId);

    @Query("SELECT i FROM Inventory i WHERE i.tenantId = :tenantId AND i.branchId = :branchId " +
           "AND i.deletedAt IS NULL AND i.quantityAvailable < i.reorderPoint")
    List<Inventory> findLowStockItemsByBranch(@Param("tenantId") String tenantId, @Param("branchId") UUID branchId);
}
