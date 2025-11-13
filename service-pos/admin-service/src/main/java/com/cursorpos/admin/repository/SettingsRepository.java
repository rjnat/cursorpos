package com.cursorpos.admin.repository;

import com.cursorpos.admin.entity.Settings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Settings entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, UUID> {

    Optional<Settings> findByTenantIdAndSettingKeyAndDeletedAtIsNull(String tenantId, String settingKey);

    List<Settings> findByTenantIdAndCategoryAndDeletedAtIsNull(String tenantId, String category);

    Page<Settings> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    boolean existsByTenantIdAndSettingKey(String tenantId, String settingKey);
}
