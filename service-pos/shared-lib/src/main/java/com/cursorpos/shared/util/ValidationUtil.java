package com.cursorpos.shared.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Utility class for validation operations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@UtilityClass
public class ValidationUtil {

    /**
     * Checks if a string is null or blank.
     * 
     * @param str the string to check
     * @return true if null or blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if a string is not blank.
     * 
     * @param str the string to check
     * @return true if not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Validates that a string is a valid UUID.
     * 
     * @param str the string to validate
     * @return true if valid UUID
     */
    public static boolean isValidUuid(String str) {
        if (isBlank(str)) {
            return false;
        }
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates email format.
     * 
     * @param email the email to validate
     * @return true if valid email
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Validates phone number (simple validation).
     * 
     * @param phone the phone number to validate
     * @return true if valid phone number
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        // Allow digits, spaces, dashes, parentheses, and plus sign
        String phoneRegex = "^[+]?[0-9\\s\\-()]+$";
        return phone.matches(phoneRegex) && phone.replaceAll("\\D", "").length() >= 10;
    }
}
