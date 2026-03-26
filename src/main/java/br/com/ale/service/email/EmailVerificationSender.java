package br.com.ale.service.email;

import br.com.ale.domain.client.Client;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class EmailVerificationSender {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final EmailService emailService;
    private final EmailTemplateService templateService;

    public EmailVerificationSender(
            EmailService emailService,
            EmailTemplateService templateService
    ) {
        this.emailService = emailService;
        this.templateService = templateService;

    }

    public void sendVerificationEmail(Client client, String token) {

        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        String rawEmail = client.getEmail();
        String email = Objects.toString(rawEmail, "").trim();

        if (email.isEmpty()) {
            throw new IllegalArgumentException("Client email is empty or null");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Client email format is invalid: [" + email + "]");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        String link = "https://api.alessandro-bezerra.me" + "/auth/verify?token=" + token;

        String html = templateService.buildVerifyEmailTemplate(
                Objects.toString(client.getName(), "User"),
                link
        );

        try {

            emailService.send(
                    email,
                    "Confirm your account",
                    html
            );

        } catch (Exception e) {
            throw new RuntimeException("Error sending email [to=" + email + "]", e);
        }
    }

    public void sendPasswordReset(Client client, String token) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        String rawEmail = client.getEmail();
        String email = Objects.toString(rawEmail, "").trim();

        if (email.isEmpty()) {
            throw new IllegalArgumentException("Client email is empty or null");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Client email format is invalid: [" + email + "]");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        String link = "https://app.alessandro-bezerra.me" + "/reset-password?token=" + token;

        String html = templateService.buildResetPasswordTemplate(
                Objects.toString(client.getName(), "User"),
                link
        );

        try {

            emailService.send(
                    email,
                    "Reset your password",
                    html
            );

        } catch (Exception e) {
            throw new RuntimeException("Error sending email [to=" + email + "]", e);
        }
    }
}