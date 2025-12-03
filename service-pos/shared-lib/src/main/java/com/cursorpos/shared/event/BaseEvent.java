package com.cursorpos.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all Kafka events.
 * 
 * <p>
 * All domain events should extend this class to ensure consistent
 * event structure with metadata for tracing and auditing.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    /**
     * Unique event identifier.
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Type of event (e.g., "TenantCreated", "ProductUpdated").
     */
    private String eventType;

    /**
     * Tenant ID for multi-tenant isolation.
     */
    private String tenantId;

    /**
     * Timestamp when event was created.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * User who triggered the event.
     */
    private String userId;

    /**
     * Optional correlation ID for request tracing.
     */
    private String correlationId;

    /**
     * Version of the event schema.
     */
    @Builder.Default
    private String version = "1.0";
}
