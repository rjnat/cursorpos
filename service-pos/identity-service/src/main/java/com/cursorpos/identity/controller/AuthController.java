package com.cursorpos.identity.controller;

import com.cursorpos.identity.dto.AuthResponse;
import com.cursorpos.identity.dto.LoginRequest;
import com.cursorpos.identity.dto.RefreshTokenRequest;
import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.service.AuthService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * 
 * <p>
 * Provides endpoints for user login, token refresh, token validation, and
 * logout.
 * All endpoints are public except validate which requires a token.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final JwtUtil jwtUtil;

        /**
         * Authenticates a user and returns JWT tokens.
         * 
         * @param request the login request
         * @return authentication response with tokens
         */
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
                log.info("Login request received for: {} (tenant: {})", request.getEmail(), request.getTenantId());

                AuthResponse authResponse = authService.login(request);

                return ResponseEntity.ok(
                                ApiResponse.success(authResponse, "Login successful"));
        }

        /**
         * Refreshes the access token using a refresh token.
         * 
         * @param request the refresh token request
         * @return new authentication response with tokens
         */
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
                log.debug("Refresh token request received");

                AuthResponse authResponse = authService.refreshToken(request);

                return ResponseEntity.ok(
                                ApiResponse.success(authResponse, "Token refreshed successfully"));
        }

        /**
         * Validates a JWT token and returns user information.
         * 
         * @param authHeader the Authorization header
         * @return user information
         */
        @GetMapping("/validate")
        public ResponseEntity<ApiResponse<UserDto>> validateToken(
                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
                log.debug("Token validation request received");

                if (authHeader == null || authHeader.isBlank()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponse.error("MISSING_AUTH_HEADER",
                                                        "Authorization header is required"));
                }

                String token = jwtUtil.extractTokenFromHeader(authHeader);
                if (token == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error("INVALID_TOKEN",
                                                        "Authorization header is missing or invalid"));
                }

                UserDto userDto = authService.validateToken(token);

                return ResponseEntity.ok(
                                ApiResponse.success(userDto, "Token is valid"));
        }

        /**
         * Logs out a user by invalidating the token.
         * 
         * @param authHeader the Authorization header
         * @return success message
         */
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader("Authorization") String authHeader) {
                log.info("Logout request received");

                String token = jwtUtil.extractTokenFromHeader(authHeader);
                if (token != null) {
                        authService.logout(token);
                }

                return ResponseEntity.ok(
                                ApiResponse.success(null, "Logout successful"));
        }
}
