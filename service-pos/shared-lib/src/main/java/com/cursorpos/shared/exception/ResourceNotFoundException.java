package com.cursorpos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * <p>
 * Maps to HTTP 404 Not Found status.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
public class ResourceNotFoundException extends CursorPosException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier),
                HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
