package com.cursorpos.identity.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity methods.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId("tenant-test-001");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("hashedpassword");
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);
    }

    @Test
    @DisplayName("Should return full name")
    void testGetFullName() {
        // When
        String fullName = user.getFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return false when account is not locked")
    void testIsLocked_WhenNotLocked() {
        // Given
        user.setLockedUntil(null);

        // When & Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Should return false when lock has expired")
    void testIsLocked_WhenLockExpired() {
        // Given - lock expired 1 hour ago
        user.setLockedUntil(Instant.now().minusSeconds(3600));

        // When & Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Should return true when account is locked")
    void testIsLocked_WhenLocked() {
        // Given - lock for 1 hour in future
        user.setLockedUntil(Instant.now().plusSeconds(3600));

        // When & Then
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("Should lock account for specified minutes")
    void testLockAccount() {
        // Given
        int lockMinutes = 15;

        // When
        user.lockAccount(lockMinutes);

        // Then
        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("Should unlock account and reset failed attempts")
    void testUnlockAccount() {
        // Given
        user.setLockedUntil(Instant.now().plusSeconds(3600));
        user.setFailedLoginAttempts(5);

        // When
        user.unlockAccount();

        // Then
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("Should increment failed login attempts")
    void testIncrementFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(2);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should reset failed login attempts")
    void testResetFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(5);

        // When
        user.resetFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isZero();
    }
}
