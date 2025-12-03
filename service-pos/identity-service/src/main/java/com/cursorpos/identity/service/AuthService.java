package com.cursorpos.identity.service;

import com.cursorpos.identity.dto.AuthResponse;
import com.cursorpos.identity.dto.LoginRequest;
import com.cursorpos.identity.dto.RefreshTokenRequest;
import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.entity.User;
import com.cursorpos.identity.exception.AccountLockedException;
import com.cursorpos.identity.exception.AuthenticationException;
import com.cursorpos.identity.exception.InvalidTokenException;
import com.cursorpos.identity.repository.UserRepository;
import com.cursorpos.identity.security.JwtTokenProvider;
import com.cursorpos.shared.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for authentication operations.
 * 
 * <p>
 * Handles user login, token generation, token refresh, and logout operations.
 * Implements account lockout mechanism after failed login attempts.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    /**
     * Authenticates a user and generates JWT tokens.
     * 
     * @param request the login request
     * @return authentication response with tokens and user info
     * @throws AuthenticationException if credentials are invalid
     * @throws AccountLockedException  if account is locked
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {} in tenant: {}", request.getEmail(), request.getTenantId());

        // Find user by email and tenant
        User user = userRepository.findByEmailAndTenantId(request.getEmail(), request.getTenantId())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Check if account is locked
        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", request.getEmail());
            throw new AccountLockedException(
                    String.format("Account is locked until %s due to multiple failed login attempts",
                            user.getLockedUntil()));
        }

        // Check if account is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Login attempt for inactive account: {}", request.getEmail());
            throw new AuthenticationException("Account is inactive. Please contact administrator.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            log.warn("Failed login attempt for user: {} (invalid password)", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        // Reset failed attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
        }

        // Update last login timestamp
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Get user roles and permissions
        List<String> roles = userService.getUserRoles(user.getId());
        List<String> permissions = userService.getUserPermissions(user.getId());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                roles,
                permissions,
                null, // storeId - will be added in future if needed
                null // branchId - will be added in future if needed
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getTenantId());

        // Build user DTO
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        log.info("User logged in successfully: {} (tenant: {})", user.getEmail(), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(userDto)
                .build();
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * 
     * @param request the refresh token request
     * @return authentication response with new tokens
     * @throws InvalidTokenException if refresh token is invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refresh token request received");

        // Validate refresh token
        if (!Boolean.TRUE.equals(jwtUtil.validateToken(request.getRefreshToken()))) {
            log.warn("Invalid or expired refresh token");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        // Extract user ID and tenant ID from refresh token
        String userIdStr = jwtUtil.extractUserId(request.getRefreshToken());
        String tenantId = jwtUtil.extractTenantId(request.getRefreshToken());

        UUID userId = UUID.fromString(userIdStr);

        // Find user
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new InvalidTokenException("User not found for token"));

        // Check if account is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Refresh token attempt for inactive account: {}", user.getEmail());
            throw new AuthenticationException("Account is inactive");
        }

        // Get user roles and permissions
        List<String> roles = userService.getUserRoles(user.getId());
        List<String> permissions = userService.getUserPermissions(user.getId());

        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                roles,
                permissions,
                null,
                null);

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                user.getTenantId());

        // Build user DTO
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        log.info("Token refreshed successfully for user: {} (tenant: {})", user.getEmail(), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(userDto)
                .build();
    }

    /**
     * Validates a JWT token and returns user information.
     * 
     * @param token the JWT token
     * @return user DTO
     * @throws InvalidTokenException if token is invalid
     */
    @Transactional(readOnly = true)
    public UserDto validateToken(String token) {
        log.debug("Token validation request received");

        // Validate token
        if (!Boolean.TRUE.equals(jwtUtil.validateToken(token))) {
            log.warn("Invalid or expired token");
            throw new InvalidTokenException("Invalid or expired token");
        }

        // Extract user ID and tenant ID
        String userIdStr = jwtUtil.extractUserId(token);
        String tenantId = jwtUtil.extractTenantId(token);

        UUID userId = UUID.fromString(userIdStr);

        // Find user
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new InvalidTokenException("User not found for token"));

        // Check if account is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationException("Account is inactive");
        }

        // Get user roles and permissions
        List<String> roles = userService.getUserRoles(user.getId());
        List<String> permissions = userService.getUserPermissions(user.getId());

        return UserDto.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Logs out a user (placeholder for future token blacklisting).
     * 
     * @param token the JWT token to invalidate
     */
    public void logout(String token) {
        log.info("Logout request received");
        // In a production system, you would:
        // 1. Add token to Redis blacklist
        // 2. Set expiry to match token expiry
        // For now, client-side token removal is sufficient
        log.debug("User logged out (client-side token removal)");
    }

    /**
     * Handles failed login attempts and locks account if threshold is reached.
     * 
     * @param user the user who failed to login
     */
    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LOCK_DURATION_MINUTES);
            log.warn("Account locked due to {} failed login attempts: {}",
                    MAX_FAILED_ATTEMPTS, user.getEmail());
        }

        userRepository.save(user);
    }
}
