package br.com.ale.domain.auth;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private PasswordHasher() {

    }

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, hashedPassword);
    }
}