package com.cursorpos.shared.security;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local storage for current tenant context.
 * 
 * <p>This class provides a way to store and retrieve the current tenant ID
 * for the duration of a request. It should be set in the authentication
 * filter after JWT token validation.</p>
 * 
 * <p><b>IMPORTANT:</b> Always call {@link #clear()} in a finally block
 * to prevent memory leaks and tenant data leakage between requests.</p>
 * 
 * <p>Usage example:</p>
 * <pre>
 * try {
 *     TenantContext.setTenantId("tenant-123");
 *     // Business logic here
 * } finally {
 *     TenantContext.clear();
 * }
 * </pre>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<String> currentStore = new ThreadLocal<>();
    private static final ThreadLocal<String> currentBranch = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private TenantContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sets the current tenant ID for the current thread.
     * 
     * @param tenantId the tenant ID to set
     * @throws IllegalArgumentException if tenantId is null or empty
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        currentTenant.set(tenantId);
        log.debug("Tenant context set: {}", tenantId);
    }

    /**
     * Gets the current tenant ID for the current thread.
     * 
     * @return the current tenant ID, or null if not set
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * Sets the current user ID for the current thread.
     * 
     * @param userId the user ID to set
     */
    public static void setUserId(String userId) {
        currentUser.set(userId);
        log.debug("User context set: {}", userId);
    }

    /**
     * Gets the current user ID for the current thread.
     * 
     * @return the current user ID, or null if not set
     */
    public static String getUserId() {
        return currentUser.get();
    }

    /**
     * Sets the current store ID for the current thread.
     * 
     * @param storeId the store ID to set
     */
    public static void setStoreId(String storeId) {
        currentStore.set(storeId);
        log.debug("Store context set: {}", storeId);
    }

    /**
     * Gets the current store ID for the current thread.
     * 
     * @return the current store ID, or null if not set
     */
    public static String getStoreId() {
        return currentStore.get();
    }

    /**
     * Sets the current branch ID for the current thread.
     * 
     * @param branchId the branch ID to set
     */
    public static void setBranchId(String branchId) {
        currentBranch.set(branchId);
        log.debug("Branch context set: {}", branchId);
    }

    /**
     * Gets the current branch ID for the current thread.
     * 
     * @return the current branch ID, or null if not set
     */
    public static String getBranchId() {
        return currentBranch.get();
    }

    /**
     * Clears all context data for the current thread.
     * <b>MUST</b> be called in a finally block to prevent memory leaks.
     */
    public static void clear() {
        currentTenant.remove();
        currentUser.remove();
        currentStore.remove();
        currentBranch.remove();
        log.debug("Tenant context cleared");
    }

    /**
     * Checks if tenant context is set for the current thread.
     * 
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean isSet() {
        return currentTenant.get() != null;
    }

    /**
     * Gets the current tenant ID and throws exception if not set.
     * 
     * @return the current tenant ID
     * @throws IllegalStateException if tenant context is not set
     */
    public static String requireTenantId() {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantId;
    }
}
