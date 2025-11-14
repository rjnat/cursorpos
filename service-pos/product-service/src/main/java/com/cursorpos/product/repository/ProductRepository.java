package com.cursorpos.product.repository;

import com.cursorpos.product.entity.Product;
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
 * Repository for Product entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, String tenantId);

    Optional<Product> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Optional<Product> findByTenantIdAndSkuAndDeletedAtIsNull(String tenantId, String sku);

    Optional<Product> findByTenantIdAndBarcodeAndDeletedAtIsNull(String tenantId, String barcode);

    Page<Product> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndCategoryIdAndDeletedAtIsNull(String tenantId, UUID categoryId, Pageable pageable);

    List<Product> findByTenantIdAndIsActiveAndDeletedAtIsNull(String tenantId, Boolean isActive);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("tenantId") String tenantId,
            @Param("search") String search,
            Pageable pageable);

    boolean existsByTenantIdAndCode(String tenantId, String code);

    boolean existsByTenantIdAndSku(String tenantId, String sku);
}
