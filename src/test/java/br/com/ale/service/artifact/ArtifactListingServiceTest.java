package br.com.ale.service.artifact;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactListingServiceTest {

  private TestConnectionProvider provider;

  private AccountService accountService;
  private ArtifactService assetService;
  private ArtifactUnitService artifactUnitService;
  private ArtifactListingService artifactListingService;
  private ArtifactWebhookNotifier webhookNotifier;
  private ClientService clientService;
  private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;

  private long sellerAccountId;

  @BeforeEach
  void setup() {
    provider = new TestConnectionProvider();
    webhookNotifier = new ArtifactWebhookNotifier("", false);
    inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
    accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
    assetService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
    artifactListingService = new ArtifactListingService(provider);
    clientService = new ClientService(provider);
    cleanDatabase();
    sellerAccountId = createAccount();
  }

  private void cleanDatabase() {
    try (var conn = provider.getConnection();
        var stmt = conn.createStatement()) {

      stmt.execute("DELETE FROM artifact_price_history");
      stmt.execute("DELETE FROM artifact_transfer");
      stmt.execute("DELETE FROM artifact_listing");
      stmt.execute("DELETE FROM artifact_unit");
      stmt.execute("DELETE FROM artifact");
      stmt.execute("DELETE FROM transactions");
      stmt.execute("DELETE FROM account");
      stmt.execute("DELETE FROM credential");
      stmt.execute("DELETE FROM client");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private long createAccount() {
    Client client =
        clientService.createClient(
            new CreateClientRequest(
                "John", "John@mail.com", "123", Provider.LOCAL, null, false, null));

    Account account =
        accountService.createAccount(
            new CreateAccountRequest(
                client.getId(), "999999999", AccountType.DEFAULT, AccountStatus.ACTIVE));

    return account.getId();
  }

  private Artifact createAsset() {
    return assetService.createAsset(
        new CreateArtifactRequest(Map.of("name", "Cool Artifact", "rarity", "Common"), 10));
  }

  private ArtifactUnit createArtifactUnit(long artifactId) {
    return artifactUnitService.createArtifactUnit(
        new CreateArtifactUnitRequest(artifactId, sellerAccountId));
  }

  @Test
  void shouldCreateArtifactOffer() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createArtifactUnit(artifact.getId());

    ArtifactListing listing =
        artifactListingService.createArtifactOffer(
            new CreateArtifactListingRequest(
                unity.getId(),
                sellerAccountId,
                new BigDecimal("100.00"),
                ArtifactListingStatus.ACTIVE));

    assertNotNull(listing);
    assertTrue(listing.getId() > 0);
    assertEquals(unity.getId(), listing.getArtifactUnitId());
    assertEquals(sellerAccountId, listing.getSellerAccountId());
    assertEquals(0, listing.getPrice().compareTo(new BigDecimal("100.00")));
    assertEquals(ArtifactListingStatus.ACTIVE, listing.getStatus());
    assertNotNull(listing.getCreatedAt());
  }

  @Test
  void shouldSelectArtifactListingById() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createArtifactUnit(artifact.getId());

    ArtifactListing created =
        artifactListingService.createArtifactOffer(
            new CreateArtifactListingRequest(
                unity.getId(),
                sellerAccountId,
                new BigDecimal("50.00"),
                ArtifactListingStatus.ACTIVE));

    ArtifactListing found = artifactListingService.selectById(created.getId());

    assertEquals(created.getId(), found.getId());
    assertEquals(created.getArtifactUnitId(), found.getArtifactUnitId());
    assertEquals(created.getSellerAccountId(), found.getSellerAccountId());
  }

  @Test
  void shouldFailWhenArtifactListingNotFound() {

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> artifactListingService.selectById(9999L));

    assertTrue(ex.getMessage().contains("Service error while selecting artifact listing"));
    assertNotNull(ex.getCause());
    assertTrue(ex.getCause().getMessage().contains("Artifact listing not found"));
  }

  @Test
  void shouldSelectArtifactListingsByStatus() {

    Artifact artifact = createAsset();

    ArtifactUnit unity1 = createArtifactUnit(artifact.getId());
    ArtifactUnit unity2 = createArtifactUnit(artifact.getId());

    artifactListingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            unity1.getId(),
            sellerAccountId,
            new BigDecimal("10.00"),
            ArtifactListingStatus.ACTIVE));

    artifactListingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            unity2.getId(),
            sellerAccountId,
            new BigDecimal("20.00"),
            ArtifactListingStatus.ACTIVE));

    List<ArtifactListing> listings =
        artifactListingService.selectByStatus(ArtifactListingStatus.ACTIVE);

    assertEquals(2, listings.size());
    assertTrue(listings.stream().allMatch(l -> l.getStatus() == ArtifactListingStatus.ACTIVE));
  }

  @Test
  void shouldFailWhenArtifactUnitNotOwnedBySeller() {

    assertThrows(
        RuntimeException.class,
        () ->
            artifactListingService.createArtifactOffer(
                new CreateArtifactListingRequest(
                    9999L,
                    sellerAccountId,
                    new BigDecimal("100.00"),
                    ArtifactListingStatus.ACTIVE)));

    List<ArtifactListing> listings =
        artifactListingService.selectByStatus(ArtifactListingStatus.ACTIVE);

    assertTrue(listings.isEmpty());
  }
}
