package com.cursorpos.identity.exception;

import com.cursorpos.shared.exception.CursorPosException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user account is locked.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
public class AccountLockedException extends CursorPosException {

    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED");
    }
}
