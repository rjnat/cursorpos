package com.cursorpos.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration.
 * 
 * <p>
 * Configures rate limiting based on tenant ID to prevent abuse
 * and ensure fair resource usage across tenants.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Configuration
public class RateLimitConfiguration {

    /**
     * Rate limit key resolver based on tenant ID.
     * Falls back to IP address if tenant is not authenticated.
     */
    @Bean
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            // Try to get tenant ID from header (set by authentication filter)
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");

            if (tenantId != null && !tenantId.isBlank()) {
                return Mono.just(tenantId);
            }

            // Fallback to IP address for unauthenticated requests
            String ipAddress = "unknown";
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                ipAddress = remoteAddress.getAddress().getHostAddress();
            }

            return Mono.just(ipAddress);
        };
    }
}
