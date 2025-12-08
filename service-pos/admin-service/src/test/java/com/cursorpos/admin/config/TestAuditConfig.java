package com.cursorpos.admin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Test configuration for JPA auditing.
 * Overrides production auditor with test-specific implementation.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@TestConfiguration
public class TestAuditConfig {

    @Bean
    @Primary
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test-user");
    }
}
