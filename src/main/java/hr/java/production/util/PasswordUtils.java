package hr.java.production.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordUtils {
    private PasswordUtils() {}

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** Hash a plain text password */
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** verify plain text password agasint stored BCrypt hash. */
    public static boolean verify(String rawPassword, String storedHash) {
        return ENCODER.matches(rawPassword, storedHash);
    }
}