package com.cursorpos.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application.
 * 
 * <p>
 * Entry point for all client requests to the CursorPOS microservices.
 * Provides routing, load balancing, authentication, rate limiting,
 * and circuit breaking.
 * </p>
 * 
 * <p>
 * Runs on port 8080 (default).
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@SpringBootApplication(scanBasePackages = { "com.cursorpos.gateway", "com.cursorpos.shared" })
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
