package com.cursorpos.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for CursorPOS application.
 * 
 * <p>All custom exceptions should extend this class to ensure
 * consistent error handling and HTTP status code mapping.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Getter
public class CursorPosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP status code for this exception.
     */
    private final HttpStatus status;

    /**
     * Error code for client-side handling.
     */
    private final String errorCode;

    /**
     * Creates a new exception with message and default status (500).
     * 
     * @param message the error message
     */
    public CursorPosException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    /**
     * Creates a new exception with message and status.
     * 
     * @param message the error message
     * @param status the HTTP status code
     */
    public CursorPosException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    /**
     * Creates a new exception with all parameters.
     * 
     * @param message the error message
     * @param status the HTTP status code
     * @param errorCode the error code
     */
    public CursorPosException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new exception with cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param status the HTTP status code
     * @param errorCode the error code
     */
    public CursorPosException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
