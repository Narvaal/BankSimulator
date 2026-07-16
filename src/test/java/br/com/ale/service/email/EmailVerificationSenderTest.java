package br.com.ale.service.email;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmailVerificationSenderTest {

  private static class RecordingEmailService implements EmailService {
    record Sent(String to, String subject, String html) {}

    final List<Sent> sent = new ArrayList<>();
    boolean fail = false;

    @Override
    public void send(String to, String subject, String htmlBody) {
      if (fail) {
        throw new RuntimeException("smtp down");
      }
      sent.add(new Sent(to, subject, htmlBody));
    }
  }

  private final RecordingEmailService emailService = new RecordingEmailService();
  private final EmailVerificationSender sender =
      new EmailVerificationSender(emailService, new EmailTemplateService());

  private Client client(String email) {
    return new Client(1L, "John Doe", email, "hash", Provider.LOCAL, null, false, null);
  }

  @Test
  void shouldSendVerificationEmailWithLinkAndName() {
    sender.sendVerificationEmail(client("john@test.com"), "tok-123");

    assertEquals(1, emailService.sent.size());
    var sent = emailService.sent.get(0);
    assertEquals("john@test.com", sent.to());
    assertEquals("Confirm your account", sent.subject());
    assertTrue(sent.html().contains("John Doe"));
    assertTrue(sent.html().contains("/auth/verify?token=tok-123"));
  }

  @Test
  void shouldSendPasswordResetEmail() {
    sender.sendPasswordReset(client("john@test.com"), "tok-reset");

    var sent = emailService.sent.get(0);
    assertEquals("Reset your password", sent.subject());
    assertTrue(sent.html().contains("/reset-password?token=tok-reset"));
  }

  @Test
  void shouldRejectNullClientOrBlankToken() {
    assertThrows(IllegalArgumentException.class, () -> sender.sendVerificationEmail(null, "tok"));
    assertThrows(IllegalArgumentException.class, () -> sender.sendPasswordReset(null, "tok"));
    assertThrows(
        IllegalArgumentException.class,
        () -> sender.sendVerificationEmail(client("john@test.com"), " "));
    assertThrows(
        IllegalArgumentException.class,
        () -> sender.sendPasswordReset(client("john@test.com"), null));
  }

  @Test
  void shouldRejectInvalidEmailFormat() {
    assertThrows(
        IllegalArgumentException.class,
        () -> sender.sendVerificationEmail(client("not-an-email"), "tok"));
    assertThrows(
        IllegalArgumentException.class,
        () -> sender.sendPasswordReset(client("not-an-email"), "tok"));
  }

  @Test
  void shouldWrapTransportFailures() {
    emailService.fail = true;

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> sender.sendVerificationEmail(client("john@test.com"), "tok"));
    assertTrue(ex.getMessage().contains("Error sending email"));

    assertThrows(
        RuntimeException.class, () -> sender.sendPasswordReset(client("john@test.com"), "tok"));
  }
}
