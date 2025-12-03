package com.cursorpos.identity.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthenticationException.
 */
@DisplayName("AuthenticationException Tests")
class AuthenticationExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void testConstructorWithMessage() {
        // Given
        String message = "Authentication failed";

        // When
        AuthenticationException exception = new AuthenticationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "Authentication failed";
        Throwable cause = new RuntimeException("Token expired");

        // When
        AuthenticationException exception = new AuthenticationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }
}
