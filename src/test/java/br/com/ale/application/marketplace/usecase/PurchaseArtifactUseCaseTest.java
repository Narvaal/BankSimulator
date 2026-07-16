package br.com.ale.application.marketplace.usecase;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.marketplace.command.PurchaseArtifactCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.*;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import br.com.ale.service.marketplace.ArtifactPurchaseService;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PurchaseArtifactUseCaseTest {

  private TestConnectionProvider provider;

  private ClientService clientService;
  private AccountService accountService;
  private ArtifactService assetService;
  private ArtifactUnitService artifactUnitService;
  private ArtifactListingService artifactListingService;
  private ArtifactWebhookNotifier webhookNotifier;
  private ArtifactPurchaseService artifactPurchaseService;
  private ArtifactPriceHistoryService artifactPriceHistoryService;
  private JwtService jwtService;
  private PurchaseArtifactUseCase useCase;

  @BeforeEach
  void setup() {

    provider = new TestConnectionProvider();
    webhookNotifier = new ArtifactWebhookNotifier("", false);
    jwtService = createTestJwtService();

    clientService = new ClientService(provider);
    accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

    assetService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
    artifactListingService = new ArtifactListingService(provider);

    artifactPurchaseService = new ArtifactPurchaseService(provider, webhookNotifier);
    artifactPriceHistoryService = new ArtifactPriceHistoryService(provider);

    useCase =
        new PurchaseArtifactUseCase(
            accountService,
            artifactListingService,
            artifactPurchaseService,
            artifactPriceHistoryService,
            jwtService);

    cleanDatabase();
  }

  private JwtService createTestJwtService() {
    JwtService service = new JwtService();
    String secret =
        Base64.getEncoder()
            .encodeToString("test-secret-key-for-unit-tests!!".getBytes(StandardCharsets.UTF_8));
    ReflectionTestUtils.setField(service, "secretKey", secret);
    ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);
    return service;
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

  @Test
  void shouldPurchaseArtifactSuccessfully() {

    Client sellerClient = createClient();
    Client buyerClient = createClient();

    Account seller = createAccount(sellerClient);
    Account buyer = createAccount(buyerClient);
    accountService.credit(buyer.getAccountNumber(), new BigDecimal("100.00"));

    ArtifactUnit unity = createArtifactUnit(seller);
    ArtifactListing listing = createActiveListing(unity, seller);

    String buyerToken = jwtService.generateToken(buyerClient.getId());

    ArtifactPurchase purchase =
        useCase.execute(new PurchaseArtifactCommand(listing.getId(), buyerToken));

    assertNotNull(purchase);

    ArtifactListing updatedListing = artifactListingService.selectById(listing.getId());
    assertEquals(ArtifactListingStatus.SOLD, updatedListing.getStatus());
  }

  @Test
  void shouldFailWhenTokenIsInvalid() {

    Client sellerClient = createClient();
    Account seller = createAccount(sellerClient);

    ArtifactUnit unity = createArtifactUnit(seller);
    ArtifactListing listing = createActiveListing(unity, seller);

    assertThrows(
        UnauthorizedOperationException.class,
        () -> useCase.execute(new PurchaseArtifactCommand(listing.getId(), "token.invalido")));
  }

  @Test
  void shouldFailWhenSellerTriesToBuyOwnListing() {

    Client sellerClient = createClient();
    Account seller = createAccount(sellerClient);

    ArtifactUnit unity = createArtifactUnit(seller);
    ArtifactListing listing = createActiveListing(unity, seller);

    String sellerToken = jwtService.generateToken(sellerClient.getId());

    assertThrows(
        UnauthorizedOperationException.class,
        () -> useCase.execute(new PurchaseArtifactCommand(listing.getId(), sellerToken)));
  }

  private Client createClient() {
    return clientService.createClient(
        new CreateClientRequest(
            "Client " + System.nanoTime(),
            "email-" + System.nanoTime() + "@test.com",
            "pass",
            Provider.LOCAL,
            null,
            false,
            null));
  }

  private Account createAccount(Client client) {
    return accountService.createAccount(
        new CreateAccountRequest(
            client.getId(), "ACC-" + System.nanoTime(), AccountType.DEFAULT, AccountStatus.ACTIVE));
  }

  private ArtifactUnit createArtifactUnit(Account owner) {

    Artifact artifact =
        assetService.createAsset(
            new CreateArtifactRequest(
                Map.of("name", "Artifact " + System.nanoTime(), "rarity", "Common"), 1));

    return artifactUnitService.createArtifactUnit(
        new CreateArtifactUnitRequest(artifact.getId(), owner.getId()));
  }

  private ArtifactListing createActiveListing(ArtifactUnit unity, Account owner) {
    return artifactListingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            unity.getId(), owner.getId(), new BigDecimal("100.00"), ArtifactListingStatus.ACTIVE));
  }
}
