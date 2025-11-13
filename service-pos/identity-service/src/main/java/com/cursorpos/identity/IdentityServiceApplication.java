package com.cursorpos.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Identity Service Application.
 * 
 * <p>
 * Handles authentication, user management, roles, and permissions
 * for the CursorPOS multi-tenant system.
 * </p>
 * 
 * <p>
 * Runs on port 8081.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@SpringBootApplication(scanBasePackages = { "com.cursorpos.identity", "com.cursorpos.shared" })
@EnableJpaAuditing
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
