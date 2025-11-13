package com.cursorpos.shared.util;

import com.cursorpos.shared.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides current auditor (user) for JPA auditing.
 * 
 * <p>Extracts the current user ID from {@link TenantContext} to automatically
 * populate createdBy and updatedBy fields in entities.</p>
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Slf4j
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            String userId = TenantContext.getUserId();
            if (userId != null && !userId.isBlank()) {
                return Optional.of(userId);
            }
            log.debug("No user context available for auditing");
            return Optional.of("SYSTEM");
        } catch (Exception e) {
            log.warn("Error getting current auditor: {}", e.getMessage());
            return Optional.of("SYSTEM");
        }
    }
}
