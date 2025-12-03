package com.cursorpos.identity;

import com.cursorpos.shared.config.SecurityConfig;
import com.cursorpos.shared.security.JwtAuthenticationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

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
@SpringBootApplication
@ComponentScan(basePackages = { "com.cursorpos.identity",
        "com.cursorpos.shared" }, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
