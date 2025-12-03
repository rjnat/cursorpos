package com.cursorpos.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Gateway routes configuration.
 * 
 * <p>
 * Routes are configured via YAML in application.yml for better maintainability
 * and external configuration management. Programmatic routes are not used
 * to keep configuration in one place.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Configuration
public class GatewayConfiguration {
        // All routes configured in application.yml
        // See: spring.cloud.gateway.routes
}
