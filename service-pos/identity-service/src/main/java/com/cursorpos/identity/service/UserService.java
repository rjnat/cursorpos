package com.cursorpos.identity.service;

import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.entity.Role;
import com.cursorpos.identity.entity.User;
import com.cursorpos.identity.entity.UserRole;
import com.cursorpos.identity.repository.PermissionRepository;
import com.cursorpos.identity.repository.RolePermissionRepository;
import com.cursorpos.identity.repository.RoleRepository;
import com.cursorpos.identity.repository.UserRepository;
import com.cursorpos.identity.repository.UserRoleRepository;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for user management operations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Gets the current authenticated user.
     * 
     * @param userId the user ID from JWT token
     * @return user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("squid:S6809") // Intentional transactional method calls - all read-only
    public UserDto getCurrentUser(UUID userId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Getting current user: {} for tenant: {}", userId, tenantId);

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        List<String> roles = getUserRoles(userId);
        List<String> permissions = getUserPermissions(userId);

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
     * Gets user roles by user ID.
     * 
     * @param userId the user ID
     * @return list of role names
     */
    @Transactional(readOnly = true)
    public List<String> getUserRoles(UUID userId) {
        // Get all role IDs for this user
        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get role names
        return roleRepository.findAllById(roleIds)
                .stream()
                .map(Role::getName)
                .toList();
    }

    /**
     * Gets user permissions by user ID.
     * 
     * @param userId the user ID
     * @return list of permission strings
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(UUID userId) {
        // Get all role IDs for this user
        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all permission IDs for these roles
        List<UUID> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIdsAndTenantId(
                roleIds, TenantContext.getTenantId());

        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all permissions and format as "resource:action"
        return permissionRepository.findByIdIn(permissionIds)
                .stream()
                .map(permission -> permission.getResource() + ":" + permission.getAction())
                .distinct()
                .toList();
    }
}
