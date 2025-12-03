package com.cursorpos.identity.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for testing.
 */
class PasswordHashGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHashGeneratorTest.class);

    @Test
    void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        String password = "Test@123456";
        String hash = encoder.encode(password);

        LOGGER.info("Password: {}", password);
        LOGGER.info("BCrypt Hash: {}", hash);
        LOGGER.info("Verification: {}", encoder.matches(password, hash));

        // Also verify the existing hash
        String existingHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        LOGGER.info("Existing hash matches 'password': {}", encoder.matches("password", existingHash));
        LOGGER.info("Existing hash matches 'Test@123456': {}", encoder.matches("Test@123456", existingHash));

        // Basic assertion to make this a proper test utility run
        assertTrue(encoder.matches(password, hash));
    }
}
