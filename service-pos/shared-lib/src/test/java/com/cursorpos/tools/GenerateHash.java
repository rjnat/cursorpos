package com.cursorpos.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.logging.Logger;

/**
 * Simple CLI helper for generating BCrypt hashes that align with CursorPOS
 * defaults.
 */
public final class GenerateHash {

    private static final Logger LOGGER = Logger.getLogger(GenerateHash.class.getName());
    private static final int DEFAULT_STRENGTH = 10;

    private GenerateHash() {
        // Utility class
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.severe("Usage: java com.cursorpos.tools.GenerateHash <password>");
            return;
        }

        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(DEFAULT_STRENGTH);
        String hash = encoder.encode(password);

        LOGGER.info(() -> "Hash: " + hash);
        LOGGER.info("BCrypt hash generated (password not logged for security)");
    }
}
