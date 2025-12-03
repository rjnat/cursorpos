package com.cursorpos.identity.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Should generate access token with store and branch IDs")
    void testGenerateAccessToken_WithStoreAndBranch() {
        // Given
        UUID userId = UUID.randomUUID();
        String tenantId = "tenant-test-001";
        String email = "test@example.com";
        String storeId = "store-001";
        String branchId = "branch-001";

        // When
        String token = jwtTokenProvider.generateAccessToken(
                userId,
                tenantId,
                email,
                Arrays.asList("CASHIER"),
                Arrays.asList("sales:create"),
                storeId,
                branchId);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should generate access token without store and branch IDs")
    void testGenerateAccessToken_WithoutStoreAndBranch() {
        // Given
        UUID userId = UUID.randomUUID();
        String tenantId = "tenant-test-001";
        String email = "test@example.com";

        // When
        String token = jwtTokenProvider.generateAccessToken(
                userId,
                tenantId,
                email,
                Arrays.asList("ADMIN"),
                Arrays.asList("users:manage"),
                null,
                null);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should generate refresh token")
    void testGenerateRefreshToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String tenantId = "tenant-test-001";

        // When
        String token = jwtTokenProvider.generateRefreshToken(userId, tenantId);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should return expiration time")
    void testGetExpirationTime() {
        // When
        Long expirationTime = jwtTokenProvider.getExpirationTime();

        // Then
        assertThat(expirationTime).isNotNull().isPositive();
    }

    @Test
    @DisplayName("Should return refresh expiration time")
    void testGetRefreshExpirationTime() {
        // When
        Long refreshExpirationTime = jwtTokenProvider.getRefreshExpirationTime();

        // Then
        assertThat(refreshExpirationTime).isNotNull().isPositive();
    }
}
