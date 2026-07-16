package br.com.ale.domain.emailVerification;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class EmailVerificationTest {

  @Test
  void isExpiredShouldCompareAgainstNow() {
    EmailVerification active =
        new EmailVerification(
            1L,
            2L,
            "tok",
            EmailVerificationType.EMAIL_VERIFICATION,
            Instant.now().plus(1, ChronoUnit.HOURS),
            null,
            Instant.now());
    assertFalse(active.isExpired());

    EmailVerification expired =
        new EmailVerification(
            1L,
            2L,
            "tok",
            EmailVerificationType.EMAIL_VERIFICATION,
            Instant.now().minus(1, ChronoUnit.HOURS),
            null,
            Instant.now());
    assertTrue(expired.isExpired());
  }

  @Test
  void isVerifiedShouldReflectVerifiedAt() {
    EmailVerification pending =
        new EmailVerification(
            1L,
            2L,
            "tok",
            EmailVerificationType.PASSWORD_RESET,
            Instant.now().plus(1, ChronoUnit.HOURS),
            null,
            Instant.now());
    assertFalse(pending.isVerified());

    pending.setVerifiedAt(Instant.now());
    assertTrue(pending.isVerified());
  }

  @Test
  void settersShouldMutateAllFields() {
    EmailVerification ev =
        new EmailVerification(
            1L,
            2L,
            "tok",
            EmailVerificationType.EMAIL_VERIFICATION,
            Instant.now(),
            null,
            Instant.now());

    Instant now = Instant.now();
    ev.setId(9L);
    ev.setClientId(8L);
    ev.setToken("new-tok");
    ev.setType(EmailVerificationType.PASSWORD_RESET);
    ev.setExpiresAt(now);
    ev.setCreatedAt(now);

    assertEquals(9L, ev.getId());
    assertEquals(8L, ev.getClientId());
    assertEquals("new-tok", ev.getToken());
    assertEquals(EmailVerificationType.PASSWORD_RESET, ev.getType());
    assertEquals(now, ev.getExpiresAt());
    assertEquals(now, ev.getCreatedAt());
  }
}
