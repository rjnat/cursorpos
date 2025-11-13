package com.cursorpos.gateway.config;

import com.cursorpos.gateway.filter.AuthenticationGatewayFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routes configuration.
 * 
 * <p>
 * Defines routing rules programmatically with authentication filter.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfiguration {

    private final AuthenticationGatewayFilter authenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Apply authentication filter to all routes
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**", "/api/v1/users/**", "/api/v1/roles/**", "/api/v1/permissions/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("http://localhost:8081"))

                .route("admin-service", r -> r
                        .path("/api/v1/tenants/**", "/api/v1/customers/**", "/api/v1/stores/**",
                                "/api/v1/branches/**", "/api/v1/analytics/**", "/api/v1/settings/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("http://localhost:8082"))

                .route("product-service", r -> r
                        .path("/api/v1/products/**", "/api/v1/categories/**", "/api/v1/pricing/**",
                                "/api/v1/inventory/**", "/api/v1/suppliers/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("http://localhost:8083"))

                .route("transaction-service", r -> r
                        .path("/api/v1/transactions/**", "/api/v1/sales/**", "/api/v1/payments/**",
                                "/api/v1/receipts/**", "/api/v1/discounts/**", "/api/v1/loyalty/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("http://localhost:8084"))

                .build();
    }
}
