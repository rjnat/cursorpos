package com.cursorpos.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for circuit breaker.
 * 
 * <p>Provides fallback responses when downstream services are unavailable.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/identity")
    public ResponseEntity<Map<String, Object>> identityFallback() {
        log.warn("Identity service fallback triggered - service unavailable");
        return buildFallbackResponse("Identity Service", 
                "The authentication service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminFallback() {
        log.warn("Admin service fallback triggered - service unavailable");
        return buildFallbackResponse("Admin Service", 
                "The admin service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        log.warn("Product service fallback triggered - service unavailable");
        return buildFallbackResponse("Product Service", 
                "The product service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/transaction")
    public ResponseEntity<Map<String, Object>> transactionFallback() {
        log.warn("Transaction service fallback triggered - service unavailable");
        return buildFallbackResponse("Transaction Service", 
                "The transaction service is temporarily unavailable. Please try again later.");
    }

    /**
     * Builds a standardized fallback response.
     */
    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put("service", serviceName);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
