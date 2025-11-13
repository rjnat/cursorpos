package com.cursorpos.shared.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@UtilityClass
public class DateTimeUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    /**
     * Formats an Instant to ISO-8601 string.
     * 
     * @param instant the instant to format
     * @return formatted string
     */
    public static String formatToIso(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Gets the current timestamp.
     * 
     * @return current Instant
     */
    public static Instant now() {
        return Instant.now();
    }

    /**
     * Checks if an instant is in the past.
     * 
     * @param instant the instant to check
     * @return true if in the past
     */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }

    /**
     * Checks if an instant is in the future.
     * 
     * @param instant the instant to check
     * @return true if in the future
     */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }
}
