package com.cursorpos.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for JWT token operations.
 * 
 * <p>
 * Handles JWT token parsing, validation, and claims extraction.
 * Used by authentication filters to extract tenant context from tokens.
 * </p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * Extracts the tenant ID from the JWT token.
     * 
     * @param token the JWT token
     * @return the tenant ID
     */
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenant_id", String.class));
    }

    /**
     * Extracts the user ID (subject) from the JWT token.
     * 
     * @param token the JWT token
     * @return the user ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the store ID from the JWT token.
     * 
     * @param token the JWT token
     * @return the store ID, or null if not present
     */
    public String extractStoreId(String token) {
        return extractClaim(token, claims -> claims.get("store_id", String.class));
    }

    /**
     * Extracts the branch ID from the JWT token.
     * 
     * @param token the JWT token
     * @return the branch ID, or null if not present
     */
    public String extractBranchId(String token) {
        return extractClaim(token, claims -> claims.get("branch_id", String.class));
    }

    /**
     * Extracts the user role from the JWT token.
     * 
     * @param token the JWT token
     * @return the user role
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the permissions from the JWT token.
     * 
     * @param token the JWT token
     * @return list of permissions
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> claims.get("permissions", List.class));
    }

    /**
     * Extracts the expiration date from the JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token.
     * 
     * @param token          the JWT token
     * @param claimsResolver function to extract specific claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     * 
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
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
     * Checks if the token is expired.
     * 
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validates the JWT token.
     * 
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the token from the Authorization header.
     * 
     * @param authHeader the Authorization header value
     * @return the token, or null if not found
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
