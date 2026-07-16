package br.com.ale.service.marketplace;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.*;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactPurchaseServiceTest {

  private TestConnectionProvider provider;

  private ClientService clientService;
  private AccountService accountService;
  private ArtifactService assetService;
  private ArtifactUnitService artifactUnitService;
  private ArtifactListingService artifactListingService;
  private ArtifactPurchaseService artifactPurchaseService;
  private ArtifactWebhookNotifier webhookNotifier;

  private Account seller;
  private Account buyer;

  @BeforeEach
  void setup() {
    InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
    provider = new TestConnectionProvider();
    webhookNotifier = new ArtifactWebhookNotifier("", false);

    clientService = new ClientService(provider);
    accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
    assetService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
    artifactListingService = new ArtifactListingService(provider);

    artifactPurchaseService = new ArtifactPurchaseService(provider, webhookNotifier);

    cleanDatabase();

    seller = createAccount();
    buyer = createAccount();
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

  private Account createAccount() {

    Client client =
        clientService.createClient(
            new CreateClientRequest(
                "Client-" + System.nanoTime(),
                "DOC-" + System.nanoTime(),
                "pass",
                Provider.LOCAL,
                null,
                false,
                null));

    return accountService.createAccount(
        new CreateAccountRequest(
            client.getId(), "ACC-" + System.nanoTime(), AccountType.DEFAULT, AccountStatus.ACTIVE));
  }

  private Artifact createAsset() {
    return assetService.createAsset(
        new CreateArtifactRequest(
            Map.of("name", "Cool Artifact " + System.nanoTime(), "rarity", "Common"), 1));
  }

  private ArtifactUnit createUnity(Artifact artifact, Account owner) {
    return artifactUnitService.createArtifactUnit(
        new CreateArtifactUnitRequest(artifact.getId(), owner.getId()));
  }

  private ArtifactListing createActiveListing(ArtifactUnit unity) {
    return artifactListingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            unity.getId(), seller.getId(), new BigDecimal("100.00"), ArtifactListingStatus.ACTIVE));
  }

  @Test
  void shouldPurchaseArtifactSuccessfully() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createUnity(artifact, seller);
    ArtifactListing listing = createActiveListing(unity);

    ArtifactPurchase purchase =
        artifactPurchaseService.purchase(
            new CreateArtifactPurchaseRequest(listing.getId(), buyer.getId()));

    assertNotNull(purchase);
    assertEquals(listing.getId(), purchase.getListingId());
    assertEquals(unity.getId(), purchase.getArtifactUnitId());
    assertEquals(seller.getId(), purchase.getSellerAccountId());
    assertEquals(buyer.getId(), purchase.getBuyerAccountId());
    assertEquals(0, purchase.getPrice().compareTo(new BigDecimal("100.00")));

    ArtifactListing reloaded = artifactListingService.selectById(listing.getId());

    assertEquals(ArtifactListingStatus.SOLD, reloaded.getStatus());
  }

  @Test
  void shouldFailWhenListingNotFound() {

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactPurchaseService.purchase(
                    new CreateArtifactPurchaseRequest(9999L, buyer.getId())));

    assertTrue(ex.getCause().getMessage().contains("Listing not found"));
  }

  @Test
  void shouldFailWhenListingNotActive() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createUnity(artifact, seller);
    ArtifactListing listing = createActiveListing(unity);
    artifactListingService.updateStatus(listing.getId(), ArtifactListingStatus.SOLD);

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactPurchaseService.purchase(
                    new CreateArtifactPurchaseRequest(listing.getId(), buyer.getId())));

    assertTrue(ex.getCause().getMessage().contains("Listing already sold"));
  }

  @Test
  void shouldFailWhenBuyerIsSeller() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createUnity(artifact, seller);
    ArtifactListing listing = createActiveListing(unity);

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactPurchaseService.purchase(
                    new CreateArtifactPurchaseRequest(listing.getId(), seller.getId())));

    assertTrue(ex.getCause().getMessage().contains("Buyer cannot be seller"));
  }

  @Test
  void shouldRollbackWhenTransferFails() {

    Artifact artifact = createAsset();
    ArtifactUnit unity = createUnity(artifact, seller);
    ArtifactListing listing = createActiveListing(unity);

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactPurchaseService.purchase(
                    new CreateArtifactPurchaseRequest(listing.getId(), -1L)));

    assertNotNull(ex.getMessage());

    ArtifactListing reloaded = artifactListingService.selectById(listing.getId());

    assertEquals(ArtifactListingStatus.ACTIVE, reloaded.getStatus());
  }
}
