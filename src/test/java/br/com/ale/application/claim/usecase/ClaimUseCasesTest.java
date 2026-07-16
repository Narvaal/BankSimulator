package br.com.ale.application.claim.usecase;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.claim.command.ClaimArtifactUnitCommand;
import br.com.ale.application.claim.command.GetNextClaimCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.TestJwt;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClaimUseCasesTest extends DbTestSupport {

  private ClientService clientService;
  private AccountService accountService;
  private ArtifactService artifactService;
  private ArtifactUnitService artifactUnitService;
  private JwtService jwtService;

  private ClaimArtifactUnitUseCase claimUseCase;
  private GetNextClaimUseCase nextClaimUseCase;

  @BeforeEach
  void setupUseCases() {
    clientService = new ClientService(provider);
    accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    artifactService = new ArtifactService(provider);
    artifactUnitService = new ArtifactUnitService(provider, new ArtifactWebhookNotifier("", false));
    jwtService = TestJwt.create();

    claimUseCase =
        new ClaimArtifactUnitUseCase(
            accountService, artifactUnitService, artifactService, jwtService);
    nextClaimUseCase = new GetNextClaimUseCase(accountService, jwtService);
  }

  private Client createClient() {
    return clientService.createClient(
        new CreateClientRequest("John", "john@test.com", "pass", Provider.LOCAL, null, true, null));
  }

  private Account createAccount(Client client) {
    return accountService.createAccount(
        new CreateAccountRequest(
            client.getId(), "400-000-001", AccountType.DEFAULT, AccountStatus.ACTIVE));
  }

  private Artifact createArtifact() {
    return artifactService.createAsset(
        new CreateArtifactRequest(Map.of("name", "Test Card", "rarity", "Common"), 10));
  }

  @Test
  void claimShouldMintUnitAndReturnNextClaimInstant() {
    Client client = createClient();
    Account account = createAccount(client);
    Artifact artifact = createArtifact();
    String token = jwtService.generateToken(client.getId());

    var nextClaim = claimUseCase.execute(new ClaimArtifactUnitCommand(artifact.getId(), token));

    assertNotNull(nextClaim);
    assertEquals(
        1, artifactUnitService.selectByOwnerAccount(account.getId(), 0, 10).items().size());
  }

  @Test
  void claimShouldEnforceCooldown() {
    Client client = createClient();
    createAccount(client);
    Artifact artifact = createArtifact();
    String token = jwtService.generateToken(client.getId());

    claimUseCase.execute(new ClaimArtifactUnitCommand(artifact.getId(), token));

    assertThrows(
        UnauthorizedOperationException.class,
        () -> claimUseCase.execute(new ClaimArtifactUnitCommand(artifact.getId(), token)));
  }

  @Test
  void claimShouldRejectInvalidTokenOrMissingAccount() {
    Artifact artifact = createArtifact();

    assertThrows(
        UnauthorizedOperationException.class,
        () -> claimUseCase.execute(new ClaimArtifactUnitCommand(artifact.getId(), "garbage")));

    Client clientWithoutAccount = createClient();
    String token = jwtService.generateToken(clientWithoutAccount.getId());
    assertThrows(
        UnauthorizedOperationException.class,
        () -> claimUseCase.execute(new ClaimArtifactUnitCommand(artifact.getId(), token)));
  }

  @Test
  void nextClaimShouldReturnInstantForAuthenticatedAccount() {
    Client client = createClient();
    createAccount(client);
    String token = jwtService.generateToken(client.getId());

    assertNotNull(nextClaimUseCase.execute(new GetNextClaimCommand(token)));

    assertThrows(
        UnauthorizedOperationException.class,
        () -> nextClaimUseCase.execute(new GetNextClaimCommand("garbage")));
  }
}
