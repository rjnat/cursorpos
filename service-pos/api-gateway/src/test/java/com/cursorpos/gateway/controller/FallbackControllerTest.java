package com.cursorpos.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FallbackController.
 * 
 * <p>
 * Tests circuit breaker fallback responses for all downstream services.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
@DisplayName("FallbackController Unit Tests")
class FallbackControllerTest {

    private FallbackController fallbackController;

    @BeforeEach
    void setUp() {
        fallbackController = new FallbackController();
    }

    @Test
    @DisplayName("Should return SERVICE_UNAVAILABLE for identity service fallback")
    void shouldReturnServiceUnavailableForIdentityFallback() {
        // Act
        ResponseEntity<Map<String, Object>> response = fallbackController.identityFallback();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("errorCode")).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(body.get("service")).isEqualTo("Identity Service");
        assertThat(body.get("message")).isEqualTo(
                "The authentication service is temporarily unavailable. Please try again later.");
    }

    @Test
    @DisplayName("Should return SERVICE_UNAVAILABLE for admin service fallback")
    void shouldReturnServiceUnavailableForAdminFallback() {
        // Act
        ResponseEntity<Map<String, Object>> response = fallbackController.adminFallback();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("errorCode")).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(body.get("service")).isEqualTo("Admin Service");
        assertThat(body.get("message")).isEqualTo(
                "The admin service is temporarily unavailable. Please try again later.");
    }

    @Test
    @DisplayName("Should return SERVICE_UNAVAILABLE for product service fallback")
    void shouldReturnServiceUnavailableForProductFallback() {
        // Act
        ResponseEntity<Map<String, Object>> response = fallbackController.productFallback();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("errorCode")).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(body.get("service")).isEqualTo("Product Service");
        assertThat(body.get("message")).isEqualTo(
                "The product service is temporarily unavailable. Please try again later.");
    }

    @Test
    @DisplayName("Should return SERVICE_UNAVAILABLE for transaction service fallback")
    void shouldReturnServiceUnavailableForTransactionFallback() {
        // Act
        ResponseEntity<Map<String, Object>> response = fallbackController.transactionFallback();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("errorCode")).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(body.get("service")).isEqualTo("Transaction Service");
        assertThat(body.get("message")).isEqualTo(
                "The transaction service is temporarily unavailable. Please try again later.");
    }

    @Test
    @DisplayName("Should return all required fields in fallback response")
    void shouldReturnAllRequiredFieldsInFallbackResponse() {
        // Act
        ResponseEntity<Map<String, Object>> response = fallbackController.identityFallback();

        // Assert - verify all 4 required fields are present
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKeys("success", "message", "errorCode", "service");
        assertThat(body).hasSize(4);
    }

    @Test
    @DisplayName("Should return correct HTTP status code 503")
    void shouldReturnCorrectHttpStatusCode503() {
        // Act & Assert - all fallbacks should return 503
        assertThat(fallbackController.identityFallback().getStatusCode().value()).isEqualTo(503);
        assertThat(fallbackController.adminFallback().getStatusCode().value()).isEqualTo(503);
        assertThat(fallbackController.productFallback().getStatusCode().value()).isEqualTo(503);
        assertThat(fallbackController.transactionFallback().getStatusCode().value()).isEqualTo(503);
    }
}
