package com.cursorpos.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper.
 * 
 * <p>All REST API endpoints should return this wrapper to ensure
 * consistent response structure across all services.</p>
 * 
 * <p>Usage examples:</p>
 * <pre>
 * // Success response
 * ApiResponse.success(data, "User created successfully");
 * 
 * // Error response
 * ApiResponse.error("User not found", "USER_NOT_FOUND");
 * </pre>
 * 
 * @param <T> the type of data payload
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful.
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     */
    private String message;

    /**
     * The response payload data.
     */
    private T data;

    /**
     * Error code for failed requests (e.g., "USER_NOT_FOUND").
     */
    private String errorCode;

    /**
     * Timestamp when the response was generated.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Request path that generated this response.
     */
    private String path;

    /**
     * Creates a success response with data and message.
     * 
     * @param data the response data
     * @param message success message
     * @param <T> the type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a success response with data only.
     * 
     * @param data the response data
     * @param <T> the type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Creates a success response with message only (no data).
     * 
     * @param message success message
     * @param <T> the type parameter (can be Void)
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with message and error code.
     * 
     * @param message error message
     * @param errorCode error code
     * @param <T> the type parameter (can be Void)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with message only.
     * 
     * @param message error message
     * @param <T> the type parameter (can be Void)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, "INTERNAL_ERROR");
    }

    /**
     * Sets the request path for this response.
     * 
     * @param path the request path
     * @return this ApiResponse instance
     */
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }
}
