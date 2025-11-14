package com.cursorpos.transaction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-14
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Transaction Service is running";
    }
}
