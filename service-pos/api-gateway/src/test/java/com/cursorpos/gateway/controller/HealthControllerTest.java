package com.cursorpos.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HealthController.
 * 
 * <p>
 * Tests health check and info endpoints for the API Gateway.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
@DisplayName("HealthController Unit Tests")
class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }

    @Test
    @DisplayName("Should return UP status for health endpoint")
    void shouldReturnUpStatusForHealthEndpoint() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
        assertThat(body.get("service")).isEqualTo("API Gateway");
    }

    @Test
    @DisplayName("Should include timestamp in health response")
    void shouldIncludeTimestampInHealthResponse() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Assert
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("timestamp");
        assertThat(body.get("timestamp")).isNotNull();
        assertThat(body.get("timestamp").toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return all required health fields")
    void shouldReturnAllRequiredHealthFields() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Assert
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKeys("status", "service", "timestamp");
        assertThat(body).hasSize(3);
    }

    @Test
    @DisplayName("Should return service info for info endpoint")
    void shouldReturnServiceInfoForInfoEndpoint() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.info();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("service")).isEqualTo("CursorPOS API Gateway");
        assertThat(body.get("version")).isEqualTo("1.0.0");
        assertThat(body.get("description")).isEqualTo("API Gateway for CursorPOS microservices");
    }

    @Test
    @DisplayName("Should return all required info fields")
    void shouldReturnAllRequiredInfoFields() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.info();

        // Assert
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKeys("service", "version", "description");
        assertThat(body).hasSize(3);
    }

    @Test
    @DisplayName("Should return HTTP 200 for health endpoint")
    void shouldReturnHttp200ForHealthEndpoint() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should return HTTP 200 for info endpoint")
    void shouldReturnHttp200ForInfoEndpoint() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.info();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
