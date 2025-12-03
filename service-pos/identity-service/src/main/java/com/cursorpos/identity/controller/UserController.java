package com.cursorpos.identity.controller;

import com.cursorpos.identity.dto.UserDto;
import com.cursorpos.identity.service.UserService;
import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Gets the current authenticated user's information.
     * 
     * @param authHeader the Authorization header
     * @return user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        log.debug("Get current user request received");

        String token = jwtUtil.extractTokenFromHeader(authHeader);
        String userIdStr = jwtUtil.extractUserId(token);
        UUID userId = UUID.fromString(userIdStr);

        UserDto userDto = userService.getCurrentUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(userDto));
    }
}
