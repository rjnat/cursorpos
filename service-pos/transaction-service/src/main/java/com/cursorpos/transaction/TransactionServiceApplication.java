package com.cursorpos.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Transaction Service.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@SpringBootApplication(scanBasePackages = { "com.cursorpos.transaction", "com.cursorpos.shared" })
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
