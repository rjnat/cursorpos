package com.cursorpos.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Admin Service - Manages tenants, customers, stores, branches, and system
 * settings.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@SpringBootApplication(scanBasePackages = { "com.cursorpos.admin", "com.cursorpos.shared" })
@EnableJpaAuditing
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
