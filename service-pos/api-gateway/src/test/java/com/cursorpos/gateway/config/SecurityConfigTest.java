package com.cursorpos.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SecurityConfig.
 * 
 * <p>
 * Tests security filter chain configuration for the API Gateway.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@WebFluxTest
@Import(SecurityConfig.class)
@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Should create SecurityWebFilterChain bean")
    void shouldCreateSecurityWebFilterChainBean() {
        // Assert
        assertThat(securityWebFilterChain).isNotNull();
    }

    @Test
    @DisplayName("Should permit all exchanges without authentication")
    void shouldPermitAllExchangesWithoutAuthentication() {
        // Act & Assert - any endpoint should be accessible without auth
        webTestClient.get()
                .uri("/any-path")
                .exchange()
                .expectStatus().isNotFound(); // 404 because no controller, but NOT 401/403
    }

    @Test
    @DisplayName("Should disable CSRF protection")
    void shouldDisableCsrfProtection() {
        // Act & Assert - POST request should not require CSRF token
        webTestClient.post()
                .uri("/any-path")
                .exchange()
                .expectStatus().isNotFound(); // 404 because no controller, but NOT 403 CSRF
    }

    @Test
    @DisplayName("Should allow OPTIONS requests for CORS preflight")
    void shouldAllowOptionsRequestsForCorsPreflight() {
        // Act & Assert
        webTestClient.options()
                .uri("/any-path")
                .exchange()
                .expectStatus().isNotFound(); // 404 because no controller, but NOT blocked
    }

    @Test
    @DisplayName("Should not require authorization header")
    void shouldNotRequireAuthorizationHeader() {
        // Act & Assert - request without Authorization header should pass security
        webTestClient.get()
                .uri("/test-endpoint")
                .exchange()
                .expectStatus().isNotFound(); // 404 because no controller, NOT 401
    }
}
