package com.cursorpos.identity.service;

import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.entity.Permission;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
@SuppressWarnings({ "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserRoleRepository userRoleRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private RolePermissionRepository rolePermissionRepository;

        @Mock
        private PermissionRepository permissionRepository;

        @InjectMocks
        private UserService userService;

        private UUID testUserId;
        private String testTenantId;
        private User testUser;
        private UUID testRoleId;
        private Role testRole;
        private UUID testPermissionId;
        private Permission testPermission;

        @BeforeEach
        void setUp() {
                testUserId = UUID.randomUUID();
                testTenantId = "tenant-test-001";
                TenantContext.setTenantId(testTenantId);

                testUser = new User();
                testUser.setId(testUserId);
                testUser.setTenantId(testTenantId);
                testUser.setEmail("test@example.com");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setPhone("+628123456789");
                testUser.setIsActive(true);
                testUser.setEmailVerified(true);
                testUser.setLastLoginAt(Instant.now());
                testUser.setCreatedAt(Instant.now());
                testUser.setUpdatedAt(Instant.now());

                testRoleId = UUID.randomUUID();
                testRole = new Role();
                testRole.setId(testRoleId);
                testRole.setName("CASHIER");
                testRole.setTenantId(testTenantId);

                testPermissionId = UUID.randomUUID();
                testPermission = new Permission();
                testPermission.setId(testPermissionId);
                testPermission.setResource("products");
                testPermission.setAction("read");
                testPermission.setTenantId(testTenantId);
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
        }

        @Test
        @DisplayName("Should get current user with roles and permissions")
        @SuppressWarnings("null")
        void testGetCurrentUser_WithRolesAndPermissions_ReturnsUserDto() {
                // Given
                UserRole userRole = new UserRole();
                userRole.setUserId(testUserId);
                userRole.setRoleId(testRoleId);
                userRole.setTenantId(testTenantId);

                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.of(testUser));
                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole));
                when(roleRepository.findAllById(any()))
                                .thenReturn(List.of(testRole));
                when(rolePermissionRepository.findPermissionIdsByRoleIdsAndTenantId(any(), any()))
                                .thenReturn(List.of(testPermissionId));
                when(permissionRepository.findByIdIn(any()))
                                .thenReturn(List.of(testPermission));

                // When
                UserDto result = userService.getCurrentUser(testUserId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(testUserId);
                assertThat(result.getEmail()).isEqualTo("test@example.com");
                assertThat(result.getFirstName()).isEqualTo("Test");
                assertThat(result.getLastName()).isEqualTo("User");
                assertThat(result.getTenantId()).isEqualTo(testTenantId);
                assertThat(result.getRoles()).containsExactly("CASHIER");
                assertThat(result.getPermissions()).containsExactly("products:read");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testGetCurrentUser_UserNotFound_ThrowsException() {
                // Given
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> userService.getCurrentUser(testUserId))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("User");
        }

        @Test
        @DisplayName("Should get user roles")
        @SuppressWarnings("null")
        void testGetUserRoles_WithRoles_ReturnsRoleNames() {
                // Given
                UserRole userRole = new UserRole();
                userRole.setUserId(testUserId);
                userRole.setRoleId(testRoleId);

                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole));
                when(roleRepository.findAllById(any()))
                                .thenReturn(List.of(testRole));

                // When
                List<String> result = userService.getUserRoles(testUserId);

                // Then
                assertThat(result).containsExactly("CASHIER");
        }

        @Test
        @DisplayName("Should return empty list when user has no roles")
        void testGetUserRoles_NoRoles_ReturnsEmptyList() {
                // Given
                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of());

                // When
                List<String> result = userService.getUserRoles(testUserId);

                // Then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get user permissions")
        void testGetUserPermissions_WithPermissions_ReturnsPermissionStrings() {
                // Given
                UserRole userRole = new UserRole();
                userRole.setUserId(testUserId);
                userRole.setRoleId(testRoleId);

                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole));
                when(rolePermissionRepository.findPermissionIdsByRoleIdsAndTenantId(any(), any()))
                                .thenReturn(List.of(testPermissionId));
                when(permissionRepository.findByIdIn(any()))
                                .thenReturn(List.of(testPermission));

                // When
                List<String> result = userService.getUserPermissions(testUserId);

                // Then
                assertThat(result).containsExactly("products:read");
        }

        @Test
        @DisplayName("Should return empty list when user has no roles for permissions")
        void testGetUserPermissions_NoRoles_ReturnsEmptyList() {
                // Given
                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of());

                // When
                List<String> result = userService.getUserPermissions(testUserId);

                // Then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when roles have no permissions")
        void testGetUserPermissions_NoPermissions_ReturnsEmptyList() {
                // Given
                UserRole userRole = new UserRole();
                userRole.setUserId(testUserId);
                userRole.setRoleId(testRoleId);

                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole));
                when(rolePermissionRepository.findPermissionIdsByRoleIdsAndTenantId(any(), any()))
                                .thenReturn(List.of());

                // When
                List<String> result = userService.getUserPermissions(testUserId);

                // Then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get multiple roles for user")
        @SuppressWarnings("null")
        void testGetUserRoles_MultipleRoles_ReturnsAllRoleNames() {
                // Given
                UUID secondRoleId = UUID.randomUUID();
                Role secondRole = new Role();
                secondRole.setId(secondRoleId);
                secondRole.setName("MANAGER");

                UserRole userRole1 = new UserRole();
                userRole1.setUserId(testUserId);
                userRole1.setRoleId(testRoleId);

                UserRole userRole2 = new UserRole();
                userRole2.setUserId(testUserId);
                userRole2.setRoleId(secondRoleId);

                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole1, userRole2));
                when(roleRepository.findAllById(any()))
                                .thenReturn(List.of(testRole, secondRole));

                // When
                List<String> result = userService.getUserRoles(testUserId);

                // Then
                assertThat(result).containsExactlyInAnyOrder("CASHIER", "MANAGER");
        }

        @Test
        @DisplayName("Should get multiple permissions and remove duplicates")
        void testGetUserPermissions_MultiplePermissions_ReturnsDistinctPermissions() {
                // Given
                UUID secondPermissionId = UUID.randomUUID();
                Permission secondPermission = new Permission();
                secondPermission.setId(secondPermissionId);
                secondPermission.setResource("products");
                secondPermission.setAction("write");

                UserRole userRole = new UserRole();
                userRole.setUserId(testUserId);
                userRole.setRoleId(testRoleId);

                when(userRoleRepository.findByUserId(testUserId))
                                .thenReturn(List.of(userRole));
                when(rolePermissionRepository.findPermissionIdsByRoleIdsAndTenantId(any(), any()))
                                .thenReturn(List.of(testPermissionId, secondPermissionId));
                when(permissionRepository.findByIdIn(any()))
                                .thenReturn(List.of(testPermission, secondPermission));

                // When
                List<String> result = userService.getUserPermissions(testUserId);

                // Then
                assertThat(result).containsExactlyInAnyOrder("products:read", "products:write");
        }
}
