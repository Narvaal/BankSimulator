package br.com.ale.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.support.DbTestSupport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class EmailVerificationServiceTest extends DbTestSupport {

  private EmailVerificationService service() {
    return new EmailVerificationService(provider);
  }

  private long createToken(long clientId, String token) {
    return service()
        .create(
            new CreateEmailVerificationRequest(
                clientId,
                token,
                EmailVerificationType.EMAIL_VERIFICATION,
                Instant.now().plus(1, ChronoUnit.HOURS),
                null));
  }

  @Test
  void createShouldInvalidatePreviousTokens() {
    long clientId = insertClient("John", "john@test.com");

    createToken(clientId, "tok-old");
    createToken(clientId, "tok-new");

    assertTrue(
        service().findByToken("tok-old", EmailVerificationType.EMAIL_VERIFICATION).isEmpty());
    assertTrue(
        service().findByToken("tok-new", EmailVerificationType.EMAIL_VERIFICATION).isPresent());
  }

  @Test
  void findActiveByClientIdShouldReturnLatest() {
    long clientId = insertClient("John", "john@test.com");
    createToken(clientId, "tok-active");

    EmailVerification ev =
        service()
            .findActiveByClientId(clientId, EmailVerificationType.EMAIL_VERIFICATION)
            .orElseThrow();
    assertEquals("tok-active", ev.getToken());
  }

  @Test
  void validateTokenShouldReturnOrThrow() {
    long clientId = insertClient("John", "john@test.com");
    createToken(clientId, "tok-valid");

    assertEquals(
        "tok-valid",
        service().validateToken("tok-valid", EmailVerificationType.EMAIL_VERIFICATION).getToken());

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> service().validateToken("ghost", EmailVerificationType.EMAIL_VERIFICATION));
    assertTrue(ex.getMessage().contains("Invalid or expired"));
  }

  @Test
  void confirmTokenShouldConsumeToken() {
    long clientId = insertClient("John", "john@test.com");
    createToken(clientId, "tok-confirm");

    EmailVerification ev =
        service().confirmToken("tok-confirm", EmailVerificationType.EMAIL_VERIFICATION);
    assertEquals(clientId, ev.getClientId());

    assertThrows(
        RuntimeException.class,
        () -> service().confirmToken("tok-confirm", EmailVerificationType.EMAIL_VERIFICATION));
  }

  @Test
  void createShouldFailForUnknownClient() {
    assertThrows(RuntimeException.class, () -> createToken(9999L, "tok-fk"));
  }
}
