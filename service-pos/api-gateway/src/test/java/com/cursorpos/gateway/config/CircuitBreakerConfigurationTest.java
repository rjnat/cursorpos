package com.cursorpos.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for CircuitBreakerConfiguration.
 * 
 * <p>
 * Tests circuit breaker configuration and customization.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-25
 */
@DisplayName("CircuitBreakerConfiguration Unit Tests")
class CircuitBreakerConfigurationTest {

    private CircuitBreakerConfiguration circuitBreakerConfiguration;

    @BeforeEach
    void setUp() {
        circuitBreakerConfiguration = new CircuitBreakerConfiguration();
    }

    @Test
    @DisplayName("Should create customizer bean successfully")
    void shouldCreateCustomizerBeanSuccessfully() {
        // Act
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer = circuitBreakerConfiguration
                .defaultCustomizer();

        // Assert
        assertThat(customizer).isNotNull();
    }

    @Test
    @DisplayName("Should apply customization to factory without throwing exception")
    @SuppressWarnings("deprecation")
    void shouldApplyCustomizationToFactory() {
        // Arrange
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer = circuitBreakerConfiguration
                .defaultCustomizer();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory(
                circuitBreakerRegistry, timeLimiterRegistry);

        // Act & Assert - customization should apply without errors
        assertThatCode(() -> customizer.customize(factory)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create circuit breaker from factory after customization")
    @SuppressWarnings("deprecation")
    void shouldCreateCircuitBreakerFromFactoryAfterCustomization() {
        // Arrange
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer = circuitBreakerConfiguration
                .defaultCustomizer();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory(
                circuitBreakerRegistry, timeLimiterRegistry);

        // Act
        customizer.customize(factory);
        var circuitBreaker = factory.create("test-circuit");

        // Assert
        assertThat(circuitBreaker).isNotNull();
    }

    @Test
    @DisplayName("Should return same customizer type on multiple calls")
    void shouldReturnSameCustomizerTypeOnMultipleCalls() {
        // Act
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer1 = circuitBreakerConfiguration
                .defaultCustomizer();
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer2 = circuitBreakerConfiguration
                .defaultCustomizer();

        // Assert - both should be non-null customizers (new instances)
        assertThat(customizer1).isNotNull();
        assertThat(customizer2).isNotNull();
        assertThat(customizer1.getClass()).isEqualTo(customizer2.getClass());
    }
}
