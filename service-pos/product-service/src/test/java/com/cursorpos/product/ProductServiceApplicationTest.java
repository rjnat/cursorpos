package com.cursorpos.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for ProductServiceApplication main class.
 * Ensures the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        assertThat(ProductServiceApplication.class).isNotNull();
    }
}
