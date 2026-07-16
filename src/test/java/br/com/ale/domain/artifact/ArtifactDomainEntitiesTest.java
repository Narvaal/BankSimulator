package br.com.ale.domain.artifact;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.auth.Credential;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ArtifactDomainEntitiesTest {

  @Test
  void priceHistoryShouldExposeFieldsAndAllowNullOldPrice() {
    Instant now = Instant.now();
    ArtifactPriceHistory full =
        new ArtifactPriceHistory(
            1L,
            2L,
            3L,
            new BigDecimal("5.00"),
            new BigDecimal("7.50"),
            4L,
            ReasonType.UPDATED,
            now);

    assertEquals(1L, full.getId());
    assertEquals(2L, full.getArtifactListingId());
    assertEquals(3L, full.getArtifactUnitId());
    assertEquals(new BigDecimal("5.00"), full.getOldPrice());
    assertEquals(new BigDecimal("7.50"), full.getNewPrice());
    assertEquals(4L, full.getChangedByAccountId());
    assertEquals(ReasonType.UPDATED, full.getReason());
    assertEquals(now, full.getCreatedAt());

    ArtifactPriceHistory created =
        new ArtifactPriceHistory(2L, 3L, null, BigDecimal.TEN, 4L, ReasonType.CREATED);
    assertNull(created.getId());
    assertNull(created.getOldPrice());
    assertNull(created.getCreatedAt());
  }

  @Test
  void priceHistoryShouldRejectNegativeOrMissingPrices() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ArtifactPriceHistory(
                2L, 3L, new BigDecimal("-1"), BigDecimal.TEN, 4L, ReasonType.UPDATED));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ArtifactPriceHistory(2L, 3L, null, new BigDecimal("-1"), 4L, ReasonType.UPDATED));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ArtifactPriceHistory(2L, 3L, null, null, 4L, ReasonType.UPDATED));
    assertThrows(
        NullPointerException.class,
        () -> new ArtifactPriceHistory(null, 3L, null, BigDecimal.TEN, 4L, ReasonType.UPDATED));
  }

  @Test
  void listingShouldValidatePriceRules() {
    Instant now = Instant.now();
    ArtifactListing listing =
        new ArtifactListing(
            1L, 2L, 3L, new BigDecimal("10.50"), ArtifactListingStatus.ACTIVE, now, now);

    assertEquals(1L, listing.getId());
    assertEquals(2L, listing.getArtifactUnitId());
    assertEquals(3L, listing.getSellerAccountId());
    assertEquals(ArtifactListingStatus.ACTIVE, listing.getStatus());
    assertEquals(now, listing.getCreatedAt());
    assertEquals(now, listing.getUpdatedAt());

    assertThrows(
        IllegalArgumentException.class,
        () -> new ArtifactListing(2L, 3L, null, ArtifactListingStatus.ACTIVE));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ArtifactListing(2L, 3L, BigDecimal.ZERO, ArtifactListingStatus.ACTIVE));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ArtifactListing(2L, 3L, new BigDecimal("1.999"), ArtifactListingStatus.ACTIVE));
  }

  @Test
  void accountShouldValidateInvariants() {
    Account account = new Account(1L, 2L, "111-000-001", AccountType.DEFAULT, AccountStatus.ACTIVE);
    assertEquals(2L, account.getClientId());
    assertEquals("111-000-001", account.getAccountNumber());

    assertThrows(
        IllegalArgumentException.class,
        () -> new Account(1L, 0L, "111", AccountType.DEFAULT, AccountStatus.ACTIVE));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Account(1L, 2L, " ", AccountType.DEFAULT, AccountStatus.ACTIVE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Account(
                1L,
                2L,
                "111",
                AccountType.DEFAULT,
                new BigDecimal("-1"),
                AccountStatus.ACTIVE,
                "pk",
                Instant.now()));
  }

  @Test
  void credentialShouldExposeFields() {
    Credential credential = new Credential(1L, 2L, "j@t.com", "hash");
    assertEquals(2L, credential.getClientId());
    assertEquals("j@t.com", credential.getEmail());
    assertEquals("hash", credential.getPasswordHash());
  }
}
