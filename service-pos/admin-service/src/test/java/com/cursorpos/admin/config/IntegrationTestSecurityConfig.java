package com.cursorpos.admin.config;

import com.cursorpos.shared.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test security configuration for @SpringBootTest integration tests.
 * Permits all requests and uses X-Tenant-ID header for tenant context.
 * Grants full permissions via X-Permissions header.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@TestConfiguration
public class IntegrationTestSecurityConfig {

    private static final String TEST_USER = "test-user";

    @Bean
    @Order(1)
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new TenantContextAndAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * Test auditor provider that returns test-user.
     */
    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of(TEST_USER);
    }

    /**
     * Filter to extract tenant ID from X-Tenant-ID header and permissions
     * from X-Permissions header. Sets up authentication and tenant context.
     */
    private static class TenantContextAndAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(
                @org.springframework.lang.NonNull HttpServletRequest request,
                @org.springframework.lang.NonNull HttpServletResponse response,
                @org.springframework.lang.NonNull FilterChain filterChain)
                throws ServletException, IOException {
            try {
                // Extract tenant ID
                String tenantId = request.getHeader("X-Tenant-ID");
                if (tenantId != null && !tenantId.isEmpty()) {
                    TenantContext.setTenantId(tenantId);
                }

                // Extract user ID
                String userId = request.getHeader("X-User-ID");
                if (userId != null && !userId.isEmpty()) {
                    TenantContext.setUserId(userId);
                } else {
                    TenantContext.setUserId(TEST_USER);
                }

                // Extract permissions and set up authentication
                String permissionsHeader = request.getHeader("X-Permissions");
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (permissionsHeader != null && !permissionsHeader.isEmpty()) {
                    for (String perm : permissionsHeader.split(",")) {
                        authorities.add(new SimpleGrantedAuthority(perm.trim()));
                    }
                }

                // Always authenticate for tests
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId != null ? userId : TEST_USER,
                        null,
                        authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
            }
        }
    }
}
