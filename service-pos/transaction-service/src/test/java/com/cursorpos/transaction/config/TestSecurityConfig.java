package com.cursorpos.transaction.config;

import com.cursorpos.shared.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Test security configuration that permits all requests for integration tests.
 * Configured with highest precedence to override production security.
 * Includes a filter to extract tenant ID from X-Tenant-ID header.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-20
 */
@TestConfiguration
@Import(TestAuditConfig.class)
public class TestSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new TenantContextFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * Filter to extract tenant ID from X-Tenant-ID header and set it in
     * TenantContext.
     */
    private static class TenantContextFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                @org.springframework.lang.NonNull HttpServletResponse response,
                @org.springframework.lang.NonNull FilterChain filterChain)
                throws ServletException, IOException {
            try {
                String tenantId = request.getHeader("X-Tenant-ID");
                if (tenantId != null && !tenantId.isEmpty()) {
                    TenantContext.setTenantId(tenantId);
                }
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        }
    }
}
