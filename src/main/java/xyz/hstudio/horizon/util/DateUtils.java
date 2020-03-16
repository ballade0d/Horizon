package xyz.hstudio.horizon.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATTER_PRECISE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String now(final boolean precise) {
        return precise ? FORMATTER_PRECISE.format(LocalDateTime.now()) : FORMATTER.format(LocalDateTime.now());
    }
}