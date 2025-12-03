package com.cursorpos.identity.exception;

import com.cursorpos.shared.exception.CursorPosException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when JWT token is invalid or expired.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
public class InvalidTokenException extends CursorPosException {

    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
