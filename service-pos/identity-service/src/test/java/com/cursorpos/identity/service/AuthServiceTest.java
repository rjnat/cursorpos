package com.cursorpos.identity.service;

import com.cursorpos.identity.dto.AuthResponse;
import com.cursorpos.identity.dto.LoginRequest;
import com.cursorpos.identity.dto.RefreshTokenRequest;
import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.entity.User;
import com.cursorpos.identity.exception.AccountLockedException;
import com.cursorpos.identity.exception.AuthenticationException;
import com.cursorpos.identity.exception.InvalidTokenException;
import com.cursorpos.identity.repository.RoleRepository;
import com.cursorpos.identity.repository.UserRepository;
import com.cursorpos.identity.security.JwtTokenProvider;
import com.cursorpos.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class AuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private UserService userService;

        @InjectMocks
        private AuthService authService;

        private User testUser;
        private LoginRequest loginRequest;
        private String testTenantId = "tenant-test-001";
        private UUID testUserId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setId(testUserId);
                testUser.setTenantId(testTenantId);
                testUser.setEmail("test@example.com");
                testUser.setPasswordHash("$2a$10$hashedPassword");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setIsActive(true);
                testUser.setEmailVerified(true);
                testUser.setFailedLoginAttempts(0);
                testUser.setLockedUntil(null);

                loginRequest = new LoginRequest();
                loginRequest.setEmail("test@example.com");
                loginRequest.setPassword("Test@123456");
                loginRequest.setTenantId(testTenantId);
        }

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void testLogin_WithValidCredentials_ReturnsAuthResponse() {
                // Given
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                                .thenReturn(true);
                when(userService.getUserRoles(testUserId))
                                .thenReturn(List.of("CASHIER"));
                when(userService.getUserPermissions(testUserId))
                                .thenReturn(List.of("products:read", "transactions:create"));
                when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn("access-token");
                when(jwtTokenProvider.generateRefreshToken(any(), any()))
                                .thenReturn("refresh-token");
                when(jwtTokenProvider.getExpirationTime())
                                .thenReturn(86400L);

                // When
                AuthResponse response = authService.login(loginRequest);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isEqualTo("access-token");
                assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
                assertThat(response.getTokenType()).isEqualTo("Bearer");
                assertThat(response.getExpiresIn()).isEqualTo(86400L);
                assertThat(response.getUser()).isNotNull();
                assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());

                verify(userRepository).save(testUser);
                assertThat(testUser.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void testLogin_WithInvalidEmail_ThrowsAuthenticationException() {
                // Given
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> authService.login(loginRequest))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Invalid email or password");

                verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when account is locked")
        void testLogin_WithLockedAccount_ThrowsAccountLockedException() {
                // Given
                testUser.lockAccount(30);
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));

                // When & Then
                assertThatThrownBy(() -> authService.login(loginRequest))
                                .isInstanceOf(AccountLockedException.class)
                                .hasMessageContaining("Account is locked");

                verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when account is inactive")
        void testLogin_WithInactiveAccount_ThrowsAuthenticationException() {
                // Given
                testUser.setIsActive(false);
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));

                // When & Then
                assertThatThrownBy(() -> authService.login(loginRequest))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Account is inactive");

                verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw exception and increment failed attempts with invalid password")
        void testLogin_WithInvalidPassword_IncrementsFailedAttempts() {
                // Given
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                                .thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> authService.login(loginRequest))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Invalid email or password");

                assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
                verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should lock account after 5 failed login attempts")
        void testLogin_With5FailedAttempts_LocksAccount() {
                // Given
                testUser.setFailedLoginAttempts(4); // 4 previous failures
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                                .thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> authService.login(loginRequest))
                                .isInstanceOf(AuthenticationException.class);

                assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
                assertThat(testUser.getLockedUntil()).isNotNull();
                assertThat(testUser.isLocked()).isTrue();
                verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void testLogin_AfterFailedAttempts_ResetsCounter() {
                // Given
                testUser.setFailedLoginAttempts(3);
                when(userRepository.findByEmailAndTenantId(loginRequest.getEmail(), loginRequest.getTenantId()))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
                                .thenReturn(true);
                when(userService.getUserRoles(testUserId))
                                .thenReturn(List.of("CASHIER"));
                when(userService.getUserPermissions(testUserId))
                                .thenReturn(List.of("products:read"));
                when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn("access-token");
                when(jwtTokenProvider.generateRefreshToken(any(), any()))
                                .thenReturn("refresh-token");
                when(jwtTokenProvider.getExpirationTime())
                                .thenReturn(86400L);

                // When
                authService.login(loginRequest);

                // Then
                assertThat(testUser.getFailedLoginAttempts()).isZero();
        }

        @Test
        @DisplayName("Should successfully refresh token with valid refresh token")
        void testRefreshToken_WithValidToken_ReturnsNewTokens() {
                // Given
                RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
                when(jwtUtil.validateToken(request.getRefreshToken())).thenReturn(true);
                when(jwtUtil.extractUserId(request.getRefreshToken())).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(request.getRefreshToken())).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.of(testUser));
                when(userService.getUserRoles(testUserId))
                                .thenReturn(List.of("CASHIER"));
                when(userService.getUserPermissions(testUserId))
                                .thenReturn(List.of("products:read"));
                when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn("new-access-token");
                when(jwtTokenProvider.generateRefreshToken(any(), any()))
                                .thenReturn("new-refresh-token");
                when(jwtTokenProvider.getExpirationTime())
                                .thenReturn(86400L);

                // When
                AuthResponse response = authService.refreshToken(request);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isEqualTo("new-access-token");
                assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Should throw exception with invalid refresh token")
        void testRefreshToken_WithInvalidToken_ThrowsException() {
                // Given
                RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
                when(jwtUtil.validateToken(request.getRefreshToken())).thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> authService.refreshToken(request))
                                .isInstanceOf(InvalidTokenException.class)
                                .hasMessageContaining("Invalid or expired refresh token");
        }

        @Test
        @DisplayName("Should throw exception when refreshing token for inactive user")
        void testRefreshToken_WithInactiveUser_ThrowsException() {
                // Given
                testUser.setIsActive(false);
                RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
                when(jwtUtil.validateToken(request.getRefreshToken())).thenReturn(true);
                when(jwtUtil.extractUserId(request.getRefreshToken())).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(request.getRefreshToken())).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.of(testUser));

                // When & Then
                assertThatThrownBy(() -> authService.refreshToken(request))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Account is inactive");
        }

        @Test
        @DisplayName("Should throw exception when user not found during token refresh")
        void testRefreshToken_WithUserNotFound_ThrowsException() {
                // Given
                RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
                when(jwtUtil.validateToken(request.getRefreshToken())).thenReturn(true);
                when(jwtUtil.extractUserId(request.getRefreshToken())).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(request.getRefreshToken())).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> authService.refreshToken(request))
                                .isInstanceOf(InvalidTokenException.class)
                                .hasMessageContaining("User not found for token");
        }

        @Test
        @DisplayName("Should validate token and return user info")
        void testValidateToken_WithValidToken_ReturnsUserDto() {
                // Given
                String token = "valid-token";
                when(jwtUtil.validateToken(token)).thenReturn(true);
                when(jwtUtil.extractUserId(token)).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(token)).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.of(testUser));
                when(userService.getUserRoles(testUserId))
                                .thenReturn(List.of("CASHIER"));
                when(userService.getUserPermissions(testUserId))
                                .thenReturn(List.of("products:read"));

                // When
                UserDto result = authService.validateToken(token);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(testUserId);
                assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
                assertThat(result.getRoles()).contains("CASHIER");
                assertThat(result.getPermissions()).contains("products:read");
        }

        @Test
        @DisplayName("Should throw exception when validating invalid token")
        void testValidateToken_WithInvalidToken_ThrowsException() {
                // Given
                String token = "invalid-token";
                when(jwtUtil.validateToken(token)).thenReturn(false);

                // When & Then
                assertThatThrownBy(() -> authService.validateToken(token))
                                .isInstanceOf(InvalidTokenException.class)
                                .hasMessageContaining("Invalid or expired token");
        }

        @Test
        @DisplayName("Should throw exception when validating token for inactive user")
        void testValidateToken_WithInactiveUser_ThrowsException() {
                // Given
                testUser.setIsActive(false);
                String token = "valid-token";
                when(jwtUtil.validateToken(token)).thenReturn(true);
                when(jwtUtil.extractUserId(token)).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(token)).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.of(testUser));

                // When & Then
                assertThatThrownBy(() -> authService.validateToken(token))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Account is inactive");
        }

        @Test
        @DisplayName("Should throw exception when user not found during token validation")
        void testValidateToken_WithUserNotFound_ThrowsException() {
                // Given
                String token = "valid-token";
                when(jwtUtil.validateToken(token)).thenReturn(true);
                when(jwtUtil.extractUserId(token)).thenReturn(testUserId.toString());
                when(jwtUtil.extractTenantId(token)).thenReturn(testTenantId);
                when(userRepository.findByIdAndTenantId(testUserId, testTenantId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> authService.validateToken(token))
                                .isInstanceOf(InvalidTokenException.class)
                                .hasMessageContaining("User not found for token");
        }

        @Test
        @DisplayName("Should successfully logout")
        void testLogout_CompletesSuccessfully() {
                // Given
                String token = "valid-token";

                // When & Then - should not throw exception
                assertThatCode(() -> authService.logout(token))
                                .doesNotThrowAnyException();
        }
}
