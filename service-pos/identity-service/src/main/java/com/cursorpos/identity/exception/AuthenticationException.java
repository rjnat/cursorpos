package com.cursorpos.identity.exception;

import com.cursorpos.shared.exception.CursorPosException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
public class AuthenticationException extends CursorPosException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}
