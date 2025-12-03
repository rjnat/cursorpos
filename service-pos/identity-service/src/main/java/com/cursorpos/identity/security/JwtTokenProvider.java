package com.cursorpos.identity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token provider for generating access and refresh tokens.
 * 
 * <p>
 * This class is responsible for creating JWT tokens with appropriate claims
 * including tenant context, user roles, and permissions.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration; // 24 hours in seconds

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration; // 7 days in seconds

    /**
     * Generates an access token with user details and permissions.
     * 
     * @param userId      the user ID
     * @param tenantId    the tenant ID
     * @param email       the user email
     * @param roles       the user roles
     * @param permissions the user permissions
     * @param storeId     the store ID (optional)
     * @param branchId    the branch ID (optional)
     * @return the JWT access token
     */
    public String generateAccessToken(
            UUID userId,
            String tenantId,
            String email,
            List<String> roles,
            List<String> permissions,
            String storeId,
            String branchId) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", tenantId);
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        if (storeId != null) {
            claims.put("store_id", storeId);
        }

        if (branchId != null) {
            claims.put("branch_id", branchId);
        }

        claims.put("token_type", "access");

        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(expiration);

        String token = Jwts.builder()
                .subject(userId.toString())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();

        log.debug("Generated access token for user: {} (tenant: {})", userId, tenantId);
        return token;
    }

    /**
     * Generates a refresh token.
     * 
     * @param userId   the user ID
     * @param tenantId the tenant ID
     * @return the JWT refresh token
     */
    public String generateRefreshToken(UUID userId, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenant_id", tenantId);
        claims.put("token_type", "refresh");

        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(refreshExpiration);

        String token = Jwts.builder()
                .subject(userId.toString())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();

        log.debug("Generated refresh token for user: {} (tenant: {})", userId, tenantId);
        return token;
    }

    /**
     * Gets the signing key for JWT operations.
     * 
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gets the access token expiration time in seconds.
     * 
     * @return expiration time in seconds
     */
    public Long getExpirationTime() {
        return expiration;
    }

    /**
     * Gets the refresh token expiration time in seconds.
     * 
     * @return refresh expiration time in seconds
     */
    public Long getRefreshExpirationTime() {
        return refreshExpiration;
    }
}
