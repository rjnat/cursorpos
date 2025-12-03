package com.cursorpos.gateway.filter;

import com.cursorpos.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * Unit tests for AuthenticationGatewayFilter.
 * 
 * <p>
 * Tests JWT validation, tenant context extraction, and header propagation.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationGatewayFilter Unit Tests")
class AuthenticationGatewayFilterTest {

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private GatewayFilterChain chain;

        private AuthenticationGatewayFilter filter;

        private static final String VALID_TOKEN = "valid.jwt.token";
        private static final String INVALID_TOKEN = "invalid.jwt.token";
        private static final String TENANT_ID = "tenant-gateway-001";
        private static final String USER_ID = "user-123";
        private static final String STORE_ID = "store-456";
        private static final String BRANCH_ID = "branch-789";
        private static final String ROLE = "ADMIN";

        @BeforeEach
        void setUp() {
                filter = new AuthenticationGatewayFilter(jwtUtil);
        }

        @Test
        @DisplayName("Should allow public endpoint without authentication")
        void shouldAllowPublicEndpointWithoutAuth() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/auth/login")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
                verify(jwtUtil, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("Should allow register endpoint without authentication")
        void shouldAllowRegisterEndpointWithoutAuth() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/auth/register")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should allow health endpoint without authentication")
        void shouldAllowHealthEndpointWithoutAuth() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/actuator/health")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject request with missing Authorization header")
        void shouldRejectRequestWithMissingAuthHeader() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject request with invalid Authorization header format")
        void shouldRejectRequestWithInvalidAuthHeaderFormat() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token123")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject request with expired token")
        void shouldRejectRequestWithExpiredToken() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(jwtUtil.validateToken(INVALID_TOKEN)).thenReturn(false);

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(jwtUtil).validateToken(INVALID_TOKEN);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject token without tenant_id claim")
        void shouldRejectTokenWithoutTenantId() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
                when(jwtUtil.extractTenantId(VALID_TOKEN)).thenReturn(null);

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should reject token with blank tenant_id")
        void shouldRejectTokenWithBlankTenantId() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
                when(jwtUtil.extractTenantId(VALID_TOKEN)).thenReturn("   ");

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should accept valid token and add tenant headers")
        void shouldAcceptValidTokenAndAddHeaders() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
                when(jwtUtil.extractTenantId(VALID_TOKEN)).thenReturn(TENANT_ID);
                when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(USER_ID);
                when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
                when(jwtUtil.extractStoreId(VALID_TOKEN)).thenReturn(null);
                when(jwtUtil.extractBranchId(VALID_TOKEN)).thenReturn(null);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(jwtUtil).validateToken(VALID_TOKEN);
                verify(jwtUtil).extractTenantId(VALID_TOKEN);
                verify(jwtUtil).extractUserId(VALID_TOKEN);
                verify(jwtUtil).extractRole(VALID_TOKEN);
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should add all headers including store and branch when present")
        void shouldAddAllHeadersWhenStoreAndBranchPresent() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
                when(jwtUtil.extractTenantId(VALID_TOKEN)).thenReturn(TENANT_ID);
                when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(USER_ID);
                when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
                when(jwtUtil.extractStoreId(VALID_TOKEN)).thenReturn(STORE_ID);
                when(jwtUtil.extractBranchId(VALID_TOKEN)).thenReturn(BRANCH_ID);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(jwtUtil).extractStoreId(VALID_TOKEN);
                verify(jwtUtil).extractBranchId(VALID_TOKEN);
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should handle JWT parsing exceptions")
        void shouldHandleJwtParsingExceptions() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
                when(jwtUtil.extractTenantId(VALID_TOKEN)).thenThrow(new RuntimeException("JWT parsing error"));

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should allow tenant signup endpoint")
        void shouldAllowTenantSignupEndpoint() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .post("/api/v1/tenants/signup")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
                verify(jwtUtil, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("Should allow forgot password endpoint")
        void shouldAllowForgotPasswordEndpoint() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .post("/api/v1/auth/forgot-password")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should allow refresh token endpoint")
        void shouldAllowRefreshTokenEndpoint() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .post("/api/v1/auth/refresh")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                verify(chain).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should validate token returns null Boolean")
        void shouldHandleNullValidationResult() {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest
                                .get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);
                when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(null);

                // Act
                Mono<Void> result = filter.filter(exchange, chain);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                verify(chain, never()).filter(any(ServerWebExchange.class));
        }
}
