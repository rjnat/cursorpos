package com.cursorpos.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA auditing configuration.
 * 
 * <p>Enables automatic population of audit fields in {@link com.cursorpos.shared.entity.BaseEntity}
 * such as createdAt, createdBy, updatedAt, and updatedBy.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
