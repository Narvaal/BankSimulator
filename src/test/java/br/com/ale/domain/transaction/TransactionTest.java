package br.com.ale.domain.transaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private Transaction valid() {
    return new Transaction(
        1L,
        10L,
        "111-000-001",
        20L,
        "111-000-002",
        new BigDecimal("15.50"),
        TransactionType.TRANSFERENCE,
        TransactionStatus.PENDING,
        "sig");
  }

  @Test
  void shouldExposeAllFields() {
    Transaction tx = valid();
    assertEquals(1L, tx.getId());
    assertEquals(10L, tx.getFromAccountId());
    assertEquals("111-000-001", tx.getFromAccountNumber());
    assertEquals(20L, tx.getToAccountId());
    assertEquals("111-000-002", tx.getToAccountNumber());
    assertEquals(0, tx.getAmount().compareTo(new BigDecimal("15.50")));
    assertEquals(TransactionType.TRANSFERENCE, tx.getType());
    assertEquals(TransactionStatus.PENDING, tx.getStatus());
    assertEquals("sig", tx.getSignature());
  }

  @Test
  void shouldAllowNullEndpointsForDeposits() {
    Transaction tx =
        new Transaction(
            2L,
            null,
            null,
            20L,
            "111-000-002",
            BigDecimal.TEN,
            TransactionType.DEPOSIT,
            TransactionStatus.COMPLETE,
            "sig");
    assertNull(tx.getFromAccountId());
    assertNull(tx.getFromAccountNumber());
  }

  @Test
  void shouldRejectBlankAccountNumbers() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "  ",
                20L,
                "111",
                BigDecimal.TEN,
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                "sig"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "",
                BigDecimal.TEN,
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                "sig"));
  }

  @Test
  void shouldRejectNegativeOrNullAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "222",
                new BigDecimal("-1"),
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                "sig"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "222",
                null,
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                "sig"));
  }

  @Test
  void shouldRejectMissingTypeStatusOrSignature() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "222",
                BigDecimal.TEN,
                null,
                TransactionStatus.PENDING,
                "sig"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "222",
                BigDecimal.TEN,
                TransactionType.TRANSFERENCE,
                null,
                "sig"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                1L,
                10L,
                "111",
                20L,
                "222",
                BigDecimal.TEN,
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                " "));
  }
}
