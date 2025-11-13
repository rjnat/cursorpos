package com.cursorpos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when tenant isolation is violated.
 * 
 * <p>
 * This is a critical security exception that indicates an attempt
 * to access data from another tenant.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
public class TenantIsolationException extends CursorPosException {

    private static final long serialVersionUID = 1L;

    public TenantIsolationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "TENANT_ISOLATION_VIOLATION");
    }

    public TenantIsolationException() {
        super("Access denied: Tenant isolation violation detected",
                HttpStatus.FORBIDDEN, "TENANT_ISOLATION_VIOLATION");
    }
}
