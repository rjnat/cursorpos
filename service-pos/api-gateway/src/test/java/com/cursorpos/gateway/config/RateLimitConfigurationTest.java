package com.cursorpos.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitConfiguration.
 * 
 * <p>
 * Tests tenant-based rate limiting key resolution logic.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@DisplayName("RateLimitConfiguration Unit Tests")
class RateLimitConfigurationTest {

        private RateLimitConfiguration rateLimitConfiguration;
        private KeyResolver keyResolver;

        @BeforeEach
        void setUp() {
                rateLimitConfiguration = new RateLimitConfiguration();
                keyResolver = rateLimitConfiguration.tenantKeyResolver();
        }

        @Test
        @DisplayName("Should return tenant ID when X-Tenant-Id header is present")
        void shouldReturnTenantIdWhenHeaderIsPresent() {
                // Arrange
                String tenantId = "tenant-coffee-001";
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header("X-Tenant-Id", tenantId)
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext(tenantId)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return IP address when X-Tenant-Id header is missing")
        void shouldReturnIpAddressWhenTenantHeaderIsMissing() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext("192.168.1.100")
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return IP address when X-Tenant-Id header is blank")
        void shouldReturnIpAddressWhenTenantHeaderIsBlank() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header("X-Tenant-Id", "   ")
                                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext("10.0.0.1")
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return unknown when remote address is null")
        void shouldReturnUnknownWhenRemoteAddressIsNull() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext("unknown")
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return tenant ID when header is empty string")
        void shouldReturnIpAddressWhenTenantHeaderIsEmptyString() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header("X-Tenant-Id", "")
                                .remoteAddress(new InetSocketAddress("172.16.0.1", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext("172.16.0.1")
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should prefer tenant ID over IP address")
        void shouldPreferTenantIdOverIpAddress() {
                // Arrange
                String tenantId = "tenant-retail-002";
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header("X-Tenant-Id", tenantId)
                                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext(tenantId)
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should create KeyResolver bean successfully")
        void shouldCreateKeyResolverBeanSuccessfully() {
                // Assert
                assertThat(keyResolver).isNotNull();
        }

        @Test
        @DisplayName("Should handle IPv6 address")
        void shouldHandleIpv6Address() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .remoteAddress(new InetSocketAddress("::1", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .assertNext(ip -> assertThat(ip).isNotNull())
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return unknown when InetSocketAddress has no resolved address")
        @SuppressWarnings("null")
        void shouldReturnUnknownWhenInetSocketAddressHasNoResolvedAddress() {
                // Arrange - create unresolved address (address is null)
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .remoteAddress(InetSocketAddress.createUnresolved("unknown-host", 8080))
                                .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<String> result = keyResolver.resolve(exchange);

                // Assert
                StepVerifier.create(result)
                                .expectNext("unknown")
                                .verifyComplete();
        }
}
