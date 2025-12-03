package com.cursorpos.identity.controller;

import com.cursorpos.identity.dto.AuthResponse;
import com.cursorpos.identity.dto.LoginRequest;
import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// removed unused import
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserController.
 * Tests the /api/v1/users/me endpoint with real authentication flow.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-18
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
@SuppressWarnings({ "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals", "null" })
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @Test
    @DisplayName("Should verify UserController is loaded and wired correctly")
    void testUserController_IsLoaded() {
        // Verify the controller bean is loaded
        assertThat(userController).isNotNull();
    }

    @Test
    @DisplayName("Should get current user info by calling UserController directly")
    void testGetCurrentUser_DirectCall() throws Exception {
        // Verify controller is loaded
        assertThat(userController).isNotNull();

        // Given - first login to get access token and extract user info
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

        // Directly call the controller method to test its logic
        TenantContext.setTenantId("tenant-coffee-001");
        try {
            ResponseEntity<ApiResponse<UserDto>> response = userController.getCurrentUser("Bearer " + accessToken);

            // Verify the response
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            ApiResponse<UserDto> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.isSuccess()).isTrue();
            assertThat(body.getData()).isNotNull();
            assertThat(body.getData().getEmail()).isEqualTo("cashier1@coffee.test");
            assertThat(body.getData().getFirstName()).isEqualTo("Cashier");
            assertThat(body.getData().getLastName()).isEqualTo("One");
        } finally {
            TenantContext.clear();
        }
    }
}