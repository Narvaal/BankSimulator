package br.com.ale.service.artifact;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactTransferServiceTest {

  private TestConnectionProvider provider;

  private ArtifactService assetService;
  private ArtifactUnitService artifactUnitService;
  private ArtifactTransferService artifactTransferService;
  private AccountService accountService;
  private ClientService clientService;
  private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;
  private ArtifactWebhookNotifier webhookNotifier;

  private long fromAccountId;
  private long toAccountId;

  @BeforeEach
  void setup() {
    provider = new TestConnectionProvider();
    webhookNotifier = new ArtifactWebhookNotifier("", false);

    assetService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
    artifactTransferService = new ArtifactTransferService(provider, webhookNotifier);
    inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
    accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
    clientService = new ClientService(provider);

    cleanDatabase();
    createAccounts();
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

  private void createAccounts() {
    fromAccountId = createAccount("FROM");
    toAccountId = createAccount("TO");
  }

  private long createAccount(String email) {
    Client client =
        clientService.createClient(
            new CreateClientRequest("john", email, "123", Provider.LOCAL, null, false, null));

    Account account =
        accountService.createAccount(
            new CreateAccountRequest(
                client.getId(), "999999999" + email, AccountType.DEFAULT, AccountStatus.ACTIVE));

    return account.getId();
  }

  private ArtifactUnit createArtifactUnit() {
    Artifact artifact =
        assetService.createAsset(
            new CreateArtifactRequest(Map.of("name", "Artifact", "rarity", "Common"), 1));

    return artifactUnitService.createArtifactUnit(
        new CreateArtifactUnitRequest(artifact.getId(), fromAccountId));
  }

  @Test
  void shouldCreateArtifactTransfer() {

    ArtifactUnit unity = createArtifactUnit();

    ArtifactTransfer transfer =
        artifactTransferService.createAsset(
            new CreateArtifactTransferRequest(unity.getId(), fromAccountId, toAccountId));

    assertNotNull(transfer);
    assertTrue(transfer.getId() > 0);
    assertEquals(unity.getId(), transfer.getArtifactUnitId());
    assertEquals(fromAccountId, transfer.getFromAccountId());
    assertEquals(toAccountId, transfer.getToAccountId());
    assertNotNull(transfer.getCreatedAt());
  }

  @Test
  void shouldSelectArtifactTransferById() {

    ArtifactUnit unity = createArtifactUnit();

    ArtifactTransfer created =
        artifactTransferService.createAsset(
            new CreateArtifactTransferRequest(unity.getId(), fromAccountId, toAccountId));

    ArtifactTransfer found = artifactTransferService.selectById(created.getId());

    assertEquals(created.getId(), found.getId());
    assertEquals(created.getArtifactUnitId(), found.getArtifactUnitId());
    assertEquals(created.getFromAccountId(), found.getFromAccountId());
    assertEquals(created.getToAccountId(), found.getToAccountId());
  }

  @Test
  void shouldFailWhenTransferToSameAccount() {

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactTransferService.createAsset(
                    new CreateArtifactTransferRequest(1L, fromAccountId, fromAccountId)));

    assertTrue(
        ex.getMessage().contains("Not allowed artifact transfer to the same account"),
        ex.getMessage());
  }

  @Test
  void shouldFailWhenArtifactTransferNotFound() {

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> artifactTransferService.selectById(9999L));

    assertTrue(
        ex.getMessage().contains("Service error while selecting artifact transfer"),
        ex.getMessage());
  }

  @Test
  void shouldRollbackWhenInsertFails() {

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactTransferService.createAsset(
                    new CreateArtifactTransferRequest(-1L, fromAccountId, toAccountId)));

    assertNotNull(ex.getMessage());
    assertTrue(
        ex.getMessage().contains("Service error while creating artifact Transfer"),
        ex.getMessage());
  }
}
