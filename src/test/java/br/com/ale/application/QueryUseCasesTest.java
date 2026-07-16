package br.com.ale.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.account.querry.GetAccountDetailsUseCase;
import br.com.ale.application.client.query.GetClientProfileUseCase;
import br.com.ale.application.marketplace.command.CreateArtifactUnitForAccountCommand;
import br.com.ale.application.marketplace.usecase.CreateArtifactUnitForAccountUseCase;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.artifact.ArtifactUnitStatus;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.infrastructure.auth.TokenGenerator;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.TestJwt;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QueryUseCasesTest extends DbTestSupport {

  @Test
  void getAccountDetailsShouldResolveAccountFromJwt() {
    var accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    var jwtService = TestJwt.create();
    long clientId = insertClient("John", "john@test.com");
    insertAccount(clientId, "700-000-001");

    var useCase = new GetAccountDetailsUseCase(accountService, jwtService);

    var details = useCase.execute(jwtService.generateToken(clientId));
    assertEquals("700-000-001", details.accountNumber());

    assertThrows(
        UnauthorizedOperationException.class,
        () -> useCase.execute(jwtService.generateToken(clientId + 999)));
  }

  @Test
  void getClientProfileShouldReturnNameAndEmail() {
    var clientService = new ClientService(provider);
    long clientId = insertClient("John Doe", "john@test.com");

    TokenGenerator claims =
        new TokenGenerator() {
          @Override
          public AuthToken generate(long id) {
            return new AuthToken(id, "tok", Instant.now());
          }

          @Override
          public TokenClaims validate(String token) {
            return new TokenClaims(clientId, Instant.now().plusSeconds(3600));
          }
        };

    var useCase = new GetClientProfileUseCase(clientService, new AuthService(provider, claims));

    var profile = useCase.execute("tok");
    assertEquals("John Doe", profile.name());
    assertEquals("john@test.com", profile.email());
  }

  @Test
  void createUnitForAccountShouldRequireValidToken() {
    var unitService = new ArtifactUnitService(provider, new ArtifactWebhookNotifier("", false));
    var artifactService = new ArtifactService(provider);
    var jwtService = TestJwt.create();

    long clientId = insertClient("Owner", "owner@test.com");
    long accountId = insertAccount(clientId, "700-000-002");
    Artifact artifact =
        artifactService.createAsset(new CreateArtifactRequest(Map.of("name", "Card"), 5));

    var useCase = new CreateArtifactUnitForAccountUseCase(unitService, jwtService);

    ArtifactUnit unit =
        useCase.execute(
            new CreateArtifactUnitForAccountCommand(
                artifact.getId(), accountId, jwtService.generateToken(clientId)));
    assertEquals(accountId, unit.getOwnerAccountId());

    assertThrows(
        UnauthorizedOperationException.class,
        () ->
            useCase.execute(
                new CreateArtifactUnitForAccountCommand(artifact.getId(), accountId, "garbage")));
  }

  @Test
  void artifactUnitAndTransferDomainEntitiesShouldExposeFields() {
    Instant now = Instant.now();

    ArtifactUnit full = new ArtifactUnit(1L, 2L, 3L, ArtifactUnitStatus.IN_MARKET, now, now);
    assertEquals(1L, full.getId());
    assertEquals(2L, full.getArtifactId());
    assertEquals(3L, full.getOwnerAccountId());
    assertEquals(ArtifactUnitStatus.IN_MARKET, full.getStatus());
    assertEquals(now, full.getLockedAt());
    assertEquals(now, full.getCreatedAt());

    ArtifactUnit minted = new ArtifactUnit(2L, 3L);
    assertNull(minted.getId());
    assertEquals(ArtifactUnitStatus.AVAILABLE, minted.getStatus());

    ArtifactTransfer transfer = new ArtifactTransfer(9L, 4L, 5L, 6L, now);
    assertEquals(9L, transfer.getId());
    assertEquals(4L, transfer.getArtifactUnitId());
    assertEquals(5L, transfer.getFromAccountId());
    assertEquals(6L, transfer.getToAccountId());
    assertEquals(now, transfer.getCreatedAt());

    assertNull(new ArtifactTransfer(4L, 5L, 6L).getId());
    assertThrows(IllegalArgumentException.class, () -> new ArtifactTransfer(0L, 5L, 6L));
  }
}
