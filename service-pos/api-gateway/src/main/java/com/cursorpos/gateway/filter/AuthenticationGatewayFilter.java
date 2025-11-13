package com.cursorpos.gateway.filter;

import com.cursorpos.shared.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * JWT Authentication Gateway Filter.
 * 
 * <p>
 * Validates JWT tokens for all incoming requests (except public endpoints).
 * Extracts tenant context and adds headers for downstream services.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGatewayFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/tenants/signup",
            "/actuator/health",
            "/actuator/info");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Validate JWT token
        if (Boolean.FALSE.equals(jwtUtil.validateToken(token))) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        // Validate JWT token
        if (Boolean.FALSE.equals(jwtUtil.validateToken(token))) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        try {
            // Extract claims from token
            String tenantId = jwtUtil.extractTenantId(token);
            String userId = jwtUtil.extractUserId(token);
            String storeId = jwtUtil.extractStoreId(token);
            String branchId = jwtUtil.extractBranchId(token);
            String role = jwtUtil.extractRole(token);

            if (tenantId == null || tenantId.isBlank()) {
                log.error("JWT token missing tenant_id claim");
                return onError(exchange, "Token missing tenant context", HttpStatus.UNAUTHORIZED);
            }

            log.debug("Authenticated request - Tenant: {}, User: {}, Role: {}", tenantId, userId, role);

            // Add tenant context headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Tenant-Id", tenantId)
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();

            if (storeId != null) {
                mutatedRequest = mutatedRequest.mutate()
                        .header("X-Store-Id", storeId)
                        .build();
            }

            if (branchId != null) {
                mutatedRequest = mutatedRequest.mutate()
                        .header("X-Branch-Id", branchId)
                        .build();
            }

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Checks if the path is a public endpoint.
     */
    private boolean isPublicEndpoint(String path) {
        Predicate<String> pathPredicate = PUBLIC_ENDPOINTS.stream()
                .map(pattern -> (Predicate<String>) p -> p.startsWith(pattern))
                .reduce(p -> false, Predicate::or);

        return pathPredicate.test(path);
    }

    /**
     * Returns an error response.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format(
                "{\"success\":false,\"message\":\"%s\",\"errorCode\":\"%s\"}",
                message, status.name());

        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }
}
