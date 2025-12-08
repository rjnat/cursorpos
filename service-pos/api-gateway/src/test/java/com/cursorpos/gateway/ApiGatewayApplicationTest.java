package com.cursorpos.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for ApiGatewayApplication.
 * 
 * <p>
 * Tests application context loading and main method execution.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@DisplayName("ApiGatewayApplication Integration Tests")
class ApiGatewayApplicationTest {

    @Test
    @DisplayName("Should load application context successfully")
    void shouldLoadApplicationContextSuccessfully() {
        // Context loads successfully if no exception is thrown
        // This test passes if Spring can wire up all beans
        assertThatCode(() -> {
            // Application context already loaded by @SpringBootTest
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should run main method without exception")
    void shouldRunMainMethodWithoutException() {
        // Act & Assert - main method should not throw exception
        assertThatCode(() -> ApiGatewayApplication.main(new String[] {}))
                .doesNotThrowAnyException();
    }
}
