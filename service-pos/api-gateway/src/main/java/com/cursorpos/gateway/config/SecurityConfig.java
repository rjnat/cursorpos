package com.cursorpos.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for API Gateway.
 * 
 * <p>
 * Disables authentication at gateway level - authentication is handled by
 * backend services.
 * Gateway acts as a simple router without security enforcement.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-24
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll());

        return http.build();
    }
}
