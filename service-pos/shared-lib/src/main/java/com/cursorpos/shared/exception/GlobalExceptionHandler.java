package com.cursorpos.shared.exception;

import com.cursorpos.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * 
 * <p>
 * Catches all exceptions thrown by controllers and converts them
 * to standardized {@link ApiResponse} format.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final String NON_NULL_STATUS = "status";

        /**
         * Handles CursorPosException and its subclasses.
         */
        @ExceptionHandler(CursorPosException.class)
        public ResponseEntity<ApiResponse<Void>> handleCursorPosException(
                        CursorPosException ex,
                        WebRequest request) {
                log.error("CursorPOS exception: {}", ex.getMessage(), ex);

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(), ex.getErrorCode());

                return new ResponseEntity<>(response, Objects.requireNonNull(ex.getStatus(), NON_NULL_STATUS));
        }

        /**
         * Handles ResourceNotFoundException.
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        WebRequest request) {
                log.warn("Resource not found: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>error(ex.getMessage(), "RESOURCE_NOT_FOUND");

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.NOT_FOUND, NON_NULL_STATUS));
        }

        /**
         * Handles TenantIsolationException (critical security violation).
         */
        @ExceptionHandler(TenantIsolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleTenantIsolationException(
                        TenantIsolationException ex,
                        WebRequest request) {
                log.error("SECURITY ALERT - Tenant isolation violation: {}", ex.getMessage(), ex);

                ApiResponse<Void> response = ApiResponse.<Void>error(
                                "Access denied",
                                "TENANT_ISOLATION_VIOLATION");

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.FORBIDDEN, NON_NULL_STATUS));
        }

        /**
         * Handles validation errors from @Valid annotations.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {
                log.warn("Validation failed: {}", ex.getMessage());

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                                .success(false)
                                .message("Validation failed")
                                .errorCode("VALIDATION_ERROR")
                                .data(errors)
                                .build();

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.BAD_REQUEST, NON_NULL_STATUS));
        }

        /**
         * Handles Spring Security access denied exceptions.
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
                        AccessDeniedException ex,
                        WebRequest request) {
                log.warn("Access denied: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>error(
                                "Access denied",
                                "ACCESS_DENIED");

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.FORBIDDEN, NON_NULL_STATUS));
        }

        /**
         * Handles IllegalArgumentException.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        WebRequest request) {
                log.warn("Invalid argument: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>error(
                                ex.getMessage(),
                                "INVALID_ARGUMENT");

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.BAD_REQUEST, NON_NULL_STATUS));
        }

        /**
         * Handles IllegalStateException.
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
                        IllegalStateException ex,
                        WebRequest request) {
                log.warn("Invalid state: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>error(
                                ex.getMessage(),
                                "INVALID_STATE");

                return new ResponseEntity<>(response, Objects.requireNonNull(HttpStatus.BAD_REQUEST, NON_NULL_STATUS));
        }

        /**
         * Handles all other unexpected exceptions.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                        Exception ex,
                        WebRequest request) {
                log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

                ApiResponse<Void> response = ApiResponse.<Void>error(
                                "An unexpected error occurred. Please try again later.",
                                "INTERNAL_ERROR");

                return new ResponseEntity<>(response,
                                Objects.requireNonNull(HttpStatus.INTERNAL_SERVER_ERROR, NON_NULL_STATUS));
        }
}
