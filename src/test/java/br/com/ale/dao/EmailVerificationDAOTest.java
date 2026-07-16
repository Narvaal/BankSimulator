package br.com.ale.dao;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.support.DbTestSupport;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class EmailVerificationDAOTest extends DbTestSupport {

  private final EmailVerificationDAO dao = new EmailVerificationDAO();

  private long insertToken(long clientId, String token, Instant expiresAt, Instant verifiedAt) {
    try (Connection conn = open()) {
      return dao.insert(
          conn,
          new CreateEmailVerificationRequest(
              clientId, token, EmailVerificationType.EMAIL_VERIFICATION, expiresAt, verifiedAt));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldInsertAndFindValidToken() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-1", Instant.now().plus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      EmailVerification ev =
          dao.findValidByToken(conn, "tok-1", EmailVerificationType.EMAIL_VERIFICATION)
              .orElseThrow();
      assertEquals(clientId, ev.getClientId());
      assertFalse(ev.isVerified());
      assertFalse(ev.isExpired());
    }
  }

  @Test
  void shouldNotFindExpiredToken() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-expired", Instant.now().minus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      assertTrue(
          dao.findValidByToken(conn, "tok-expired", EmailVerificationType.EMAIL_VERIFICATION)
              .isEmpty());
    }
  }

  @Test
  void shouldNotFindAlreadyVerifiedToken() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-used", Instant.now().plus(1, ChronoUnit.HOURS), Instant.now());

    try (Connection conn = open()) {
      assertTrue(
          dao.findValidByToken(conn, "tok-used", EmailVerificationType.EMAIL_VERIFICATION)
              .isEmpty());
    }
  }

  @Test
  void shouldNotFindTokenOfDifferentType() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-type", Instant.now().plus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      assertTrue(
          dao.findValidByToken(conn, "tok-type", EmailVerificationType.PASSWORD_RESET).isEmpty());
    }
  }

  @Test
  void shouldFindMostRecentActiveTokenByClient() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-old", Instant.now().plus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      EmailVerification ev =
          dao.findActiveByClientId(conn, clientId, EmailVerificationType.EMAIL_VERIFICATION)
              .orElseThrow();
      assertEquals("tok-old", ev.getToken());

      assertTrue(
          dao.findActiveByClientId(conn, 9999L, EmailVerificationType.EMAIL_VERIFICATION)
              .isEmpty());
    }
  }

  @Test
  void markVerifiedShouldConsumeTokenOnce() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    long id = insertToken(clientId, "tok-mark", Instant.now().plus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      dao.markVerified(conn, id);
      assertTrue(
          dao.findValidByToken(conn, "tok-mark", EmailVerificationType.EMAIL_VERIFICATION)
              .isEmpty());

      RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.markVerified(conn, id));
      assertTrue(ex.getMessage().contains("already used"));
    }
  }

  @Test
  void invalidatePreviousTokensShouldConsumeAllActive() throws Exception {
    long clientId = insertClient("John", "john@test.com");
    insertToken(clientId, "tok-a", Instant.now().plus(1, ChronoUnit.HOURS), null);
    insertToken(clientId, "tok-b", Instant.now().plus(1, ChronoUnit.HOURS), null);

    try (Connection conn = open()) {
      dao.invalidatePreviousTokens(conn, clientId, EmailVerificationType.EMAIL_VERIFICATION);

      assertTrue(
          dao.findActiveByClientId(conn, clientId, EmailVerificationType.EMAIL_VERIFICATION)
              .isEmpty());
    }
  }
}
