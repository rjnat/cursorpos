package com.cursorpos.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter.
 * 
 * <p>
 * Intercepts all HTTP requests and validates JWT tokens from the
 * Authorization header. Extracts tenant context and sets it in
 * {@link TenantContext} for the duration of the request.
 * </p>
 * 
 * <p>
 * This filter runs once per request and ensures:
 * </p>
 * <ul>
 * <li>JWT token is valid and not expired</li>
 * <li>Tenant context is set for data isolation</li>
 * <li>User authentication is established in Spring Security context</li>
 * <li>Context is cleared after request completes</li>
 * </ul>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            final String authHeader = request.getHeader("Authorization");
            final String jwt = jwtUtil.extractTokenFromHeader(authHeader);

            if (jwt == null) {
                log.debug("No JWT token found in request");
                filterChain.doFilter(request, response);
                return;
            }

            // Validate token
            if (Boolean.FALSE.equals(jwtUtil.validateToken(jwt))) {
                log.warn("Invalid or expired JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }

            // Extract claims from token
            String userId = jwtUtil.extractUserId(jwt);
            String tenantId = jwtUtil.extractTenantId(jwt);
            String storeId = jwtUtil.extractStoreId(jwt);
            String branchId = jwtUtil.extractBranchId(jwt);
            String role = jwtUtil.extractRole(jwt);
            List<String> permissions = jwtUtil.extractPermissions(jwt);

            // Validate required fields
            if (tenantId == null || tenantId.isBlank()) {
                log.error("JWT token missing tenant_id claim");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token missing tenant context");
                return;
            }

            // Set tenant context
            TenantContext.setTenantId(tenantId);
            TenantContext.setUserId(userId);
            if (storeId != null) {
                TenantContext.setStoreId(storeId);
            }
            if (branchId != null) {
                TenantContext.setBranchId(branchId);
            }

            log.debug("Tenant context set - Tenant: {}, User: {}, Store: {}, Branch: {}",
                    tenantId, userId, storeId, branchId);

            // Set Spring Security authentication
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                List<SimpleGrantedAuthority> authorities = permissions != null
                        ? permissions.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                        : List.of();

                // Add role as authority
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authentication set for user: {} with role: {} and {} permissions",
                        userId, role, authorities.size());
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error processing JWT authentication: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication failed: " + e.getMessage());
        } finally {
            // CRITICAL: Always clear tenant context to prevent memory leaks
            TenantContext.clear();
            log.debug("Tenant context cleared after request");
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Skip authentication for public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/favicon.ico");
    }
}
