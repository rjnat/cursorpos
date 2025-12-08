package com.cursorpos.admin.config;

import com.cursorpos.shared.security.JwtAuthenticationFilter;
import com.cursorpos.shared.security.JwtUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration for WebMvcTest.
 * Enables method security to enforce @PreAuthorize annotations.
 * Configures security to work with @WithMockUser.
 * Provides mock beans for shared-lib security components.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    /**
     * Security filter chain for tests.
     * Requires authentication but allows @WithMockUser to provide principal.
     */
    @Bean
    @Primary
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    /**
     * Mock JwtUtil bean for tests.
     * Required by JwtAuthenticationFilter from shared-lib.
     */
    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return Mockito.mock(JwtUtil.class);
    }

    /**
     * Mock JwtAuthenticationFilter bean for tests.
     * Overrides the real filter from shared-lib.
     */
    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return Mockito.mock(JwtAuthenticationFilter.class);
    }
}
