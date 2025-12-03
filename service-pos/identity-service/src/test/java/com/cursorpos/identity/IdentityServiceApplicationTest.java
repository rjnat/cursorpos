package com.cursorpos.identity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for IdentityServiceApplication main class.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("IdentityServiceApplication Tests")
class IdentityServiceApplicationTest {

    @Test
    @DisplayName("Should load application context successfully")
    void testContextLoads() {
        // This test verifies that the Spring application context loads successfully
        // and all beans are properly configured
        assertThat(IdentityServiceApplication.class).isNotNull();
    }

    @Test
    @DisplayName("Should have main method that starts the application")
    void testMainMethod() {
        // Verify main method exists and can be called
        // We don't actually run it to avoid starting a full server
        assertThat(IdentityServiceApplication.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("main"));
    }
}
