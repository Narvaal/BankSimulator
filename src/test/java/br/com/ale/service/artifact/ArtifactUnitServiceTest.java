package br.com.ale.service.artifact;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.dto.CreateArtifactUnitRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactUnitServiceTest {

  private TestConnectionProvider provider;
  private ClientService clientService;
  private ArtifactService assetService;
  private AccountService accountService;
  private ArtifactUnitService artifactUnitService;
  private ArtifactWebhookNotifier webhookNotifier;
  private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;
  private long ownerAccountId;

  @BeforeEach
  void setup() {
    provider = new TestConnectionProvider();
    webhookNotifier = new ArtifactWebhookNotifier("", false);
    inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
    assetService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
    accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
    clientService = new ClientService(provider);

    cleanDatabase();
    ownerAccountId = createAccount();
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

  @Test
  void shouldCreateArtifactUnit() {

    Artifact artifact = createAsset();

    ArtifactUnit unity =
        artifactUnitService.createArtifactUnit(
            new CreateArtifactUnitRequest(artifact.getId(), ownerAccountId));

    assertNotNull(unity);
    assertTrue(unity.getId() > 0);
    assertEquals(artifact.getId(), unity.getArtifactId());
    assertEquals(ownerAccountId, unity.getOwnerAccountId());
    assertNotNull(unity.getCreatedAt());
  }

  @Test
  void shouldSelectArtifactUnitById() {

    Artifact artifact = createAsset();

    ArtifactUnit created =
        artifactUnitService.createArtifactUnit(
            new CreateArtifactUnitRequest(artifact.getId(), ownerAccountId));

    ArtifactUnit found = artifactUnitService.selectById(created.getId());

    assertEquals(created.getId(), found.getId());
    assertEquals(created.getArtifactId(), found.getArtifactId());
    assertEquals(created.getOwnerAccountId(), found.getOwnerAccountId());
  }

  @Test
  void shouldFailWhenArtifactUnitNotFound() {

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> artifactUnitService.selectById(9999L));

    assertTrue(ex.getMessage().contains("9999"), ex.getMessage());
  }

  @Test
  void shouldRollbackWhenInsertFails() {

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                artifactUnitService.createArtifactUnit(
                    new CreateArtifactUnitRequest(-1L, ownerAccountId)));

    assertNotNull(ex.getMessage());
    assertTrue(
        ex.getMessage().contains("Service error while creating artifactUnit"), ex.getMessage());
  }
}
