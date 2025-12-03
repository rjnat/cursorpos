package com.cursorpos.identity.controller;

import com.cursorpos.identity.dto.AuthResponse;
import com.cursorpos.identity.dto.LoginRequest;
import com.cursorpos.identity.dto.RefreshTokenRequest;
import com.cursorpos.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithAnonymousUser
@DisplayName("AuthController Integration Tests")
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void testLogin_WithValidCredentials_Returns200() throws Exception {
                // Given
                LoginRequest request = new LoginRequest();
                request.setEmail("cashier1@coffee.test");
                request.setPassword("Test@123456");
                request.setTenantId("tenant-coffee-001");

                // When & Then
                MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").exists())
                                .andExpect(jsonPath("$.data.refreshToken").exists())
                                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.data.expiresIn").exists())
                                .andExpect(jsonPath("$.data.user.email").value("cashier1@coffee.test"))
                                .andExpect(jsonPath("$.data.user.firstName").value("Cashier"))
                                .andExpect(jsonPath("$.data.user.roles").isArray())
                                .andExpect(jsonPath("$.data.user.permissions").isArray())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                assertThat(responseBody).contains("accessToken");
        }

        @Test
        @DisplayName("Should return 401 with invalid password")
        void testLogin_WithInvalidPassword_Returns401() throws Exception {
                // Given
                LoginRequest request = new LoginRequest();
                request.setEmail("cashier1@coffee.test");
                request.setPassword("WrongPassword");
                request.setTenantId("tenant-coffee-001");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_FAILED"));
        }

        @Test
        @DisplayName("Should return 401 with invalid email")
        void testLogin_WithInvalidEmail_Returns401() throws Exception {
                // Given
                LoginRequest request = new LoginRequest();
                request.setEmail("nonexistent@coffee.test");
                request.setPassword("Test@123456");
                request.setTenantId("tenant-coffee-001");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 with invalid request (missing fields)")
        void testLogin_WithMissingFields_Returns400() throws Exception {
                // Given - missing password
                LoginRequest request = new LoginRequest();
                request.setEmail("cashier1@coffee.test");
                request.setTenantId("tenant-coffee-001");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should successfully refresh token")
        void testRefreshToken_WithValidToken_Returns200() throws Exception {
                // Given - first login to get refresh token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("cashier1@coffee.test");
                loginRequest.setPassword("Test@123456");
                loginRequest.setTenantId("tenant-coffee-001");

                MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String loginResponse = loginResult.getResponse().getContentAsString();
                ApiResponse<AuthResponse> authResponse = objectMapper.readValue(
                                loginResponse,
                                objectMapper.getTypeFactory().constructParametricType(
                                                ApiResponse.class, AuthResponse.class));
                String refreshToken = authResponse.getData().getRefreshToken();

                // When - use refresh token
                RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
                refreshRequest.setRefreshToken(refreshToken);

                // Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").exists())
                                .andExpect(jsonPath("$.data.refreshToken").exists())
                                .andExpect(jsonPath("$.data.user.email").value("cashier1@coffee.test"));
        }

        @Test
        @DisplayName("Should return 401 with invalid refresh token")
        void testRefreshToken_WithInvalidToken_Returns401() throws Exception {
                // Given
                RefreshTokenRequest request = new RefreshTokenRequest();
                request.setRefreshToken("invalid-refresh-token");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should validate token and return user info")
        void testValidateToken_WithValidToken_Returns200() throws Exception {
                // Given - first login to get access token
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("manager@coffee.test");
                loginRequest.setPassword("Test@123456");
                loginRequest.setTenantId("tenant-coffee-001");

                MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String loginResponse = loginResult.getResponse().getContentAsString();
                ApiResponse<AuthResponse> authResponse = objectMapper.readValue(
                                loginResponse,
                                objectMapper.getTypeFactory().constructParametricType(
                                                ApiResponse.class, AuthResponse.class));
                String accessToken = authResponse.getData().getAccessToken();

                // When & Then
                mockMvc.perform(get("/api/v1/auth/validate")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("manager@coffee.test"))
                                .andExpect(jsonPath("$.data.roles").isArray())
                                .andExpect(jsonPath("$.data.permissions").isArray());
        }

        @Test
        @DisplayName("Should return 400 when validating without token")
        void testValidateToken_WithoutToken_Returns400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/auth/validate"))
                                .andExpect(status().isBadRequest()); // Missing required header
        }

        @Test
        @DisplayName("Should return 400 when validating with blank Authorization header")
        void testValidateToken_WithBlankHeader_Returns400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/auth/validate")
                                .header("Authorization", ""))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 401 when validating with invalid token")
        void testValidateToken_WithInvalidToken_Returns401() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/auth/validate")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 401 when validating with malformed Authorization header")
        void testValidateToken_WithMalformedHeader_Returns401() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/auth/validate")
                                .header("Authorization", "InvalidFormat"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should successfully logout")
        void testLogout_WithValidToken_Returns200() throws Exception {
                // Given - first login
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("cashier1@coffee.test");
                loginRequest.setPassword("Test@123456");
                loginRequest.setTenantId("tenant-coffee-001");

                MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String loginResponse = loginResult.getResponse().getContentAsString();
                ApiResponse<AuthResponse> authResponse = objectMapper.readValue(
                                loginResponse,
                                objectMapper.getTypeFactory().constructParametricType(
                                                ApiResponse.class, AuthResponse.class));
                String accessToken = authResponse.getData().getAccessToken();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        @DisplayName("Should successfully logout even with malformed Authorization header")
        void testLogout_WithMalformedHeader_Returns200() throws Exception {
                // When & Then - logout should succeed even if token can't be extracted
                mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "InvalidFormat"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        @DisplayName("Should login with different tenant users")
        void testLogin_WithDifferentTenants_ReturnsCorrectData() throws Exception {
                // Given - Restaurant tenant user
                LoginRequest request = new LoginRequest();
                request.setEmail("cashier1@restaurant.test");
                request.setPassword("Test@123456");
                request.setTenantId("tenant-restaurant-001");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.user.email").value("cashier1@restaurant.test"))
                                .andExpect(jsonPath("$.data.user.tenantId").value("tenant-restaurant-001"));
        }

        @Test
        @DisplayName("Should not login with wrong tenant")
        void testLogin_WithWrongTenant_Returns401() throws Exception {
                // Given - Coffee user trying to login to Restaurant tenant
                LoginRequest request = new LoginRequest();
                request.setEmail("cashier1@coffee.test");
                request.setPassword("Test@123456");
                request.setTenantId("tenant-restaurant-001");

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }
}
