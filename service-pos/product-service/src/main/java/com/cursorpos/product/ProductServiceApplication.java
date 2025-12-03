package com.cursorpos.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product Service Application.
 * Manages products, categories, inventory, and pricing.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@SpringBootApplication(scanBasePackages = { "com.cursorpos.product", "com.cursorpos.shared" })
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
