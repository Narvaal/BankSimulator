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

        String link = "https://bankapi.alessandro-bezerra.me" + "/auth/verify?token=" + token;

        String html = templateService.buildVerifyEmailTemplate(
                Objects.toString(client.getName(), "User"),
                link
        );

        System.out.println("=== EMAIL DEBUG ===");
        System.out.println("TO: [" + email + "]");
        System.out.println("TOKEN: [" + token + "]");
        System.out.println("LINK: [" + link + "]");
        System.out.println("===================");

        try {
            emailService.send(
                    email,
                    "Confirm your account",
                    html
            );

            System.out.println("Email successfully sent to: [" + email + "]");

        } catch (Exception e) {
            System.err.println("Failed to send email");
            System.err.println("TO: [" + email + "]");
            System.err.println("ERROR: " + e.getMessage());

            if (e.getCause() != null) {
                System.err.println("ROOT CAUSE: " + e.getCause().getMessage());
            }

            throw new RuntimeException("Error sending email [to=" + email + "]", e);
        }
    }
}