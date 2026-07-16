package br.com.ale.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.application.marketplace.query.ListArtifactListingsByOwnerUseCase;
import br.com.ale.application.transaction.query.ListTransfersByAccountUseCase;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.infrastructure.auth.TokenGenerator;
import br.com.ale.infrastructure.db.HikariConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.TransactionService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.SpyPrivateKeyStorage;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.TestJwt;
import java.math.BigDecimal;
import java.security.KeyPairGenerator;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MiscUseCasesAndInfraTest extends DbTestSupport {

  private static TokenGenerator fixedClaims(long clientId) {
    return new TokenGenerator() {
      @Override
      public AuthToken generate(long id) {
        return new AuthToken(id, "tok", Instant.now());
      }

      @Override
      public TokenClaims validate(String token) {
        return new TokenClaims(clientId, Instant.now().plusSeconds(3600));
      }
    };
  }

  @Test
  void depositUseCaseShouldCreditAccountByEmail() {
    var clientService = new ClientService(provider);
    var accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    Client client =
        clientService.createClient(
            new CreateClientRequest(
                "John", "john@test.com", "p", Provider.LOCAL, null, true, null));
    accountService.createAccount(
        new br.com.ale.dto.CreateAccountRequest(
            client.getId(),
            "600-000-001",
            br.com.ale.domain.account.AccountType.DEFAULT,
            br.com.ale.domain.account.AccountStatus.ACTIVE));

    new DepositAccountUseCase(accountService, clientService)
        .execute(new DepositAccountCommand("john@test.com", "42.00"));

    assertEquals(
        0,
        accountService
            .getAccountByNumber("600-000-001")
            .getBalance()
            .compareTo(new BigDecimal("42.00")));

    assertThrows(
        RuntimeException.class,
        () ->
            new DepositAccountUseCase(accountService, clientService)
                .execute(new DepositAccountCommand("ghost@test.com", "1.00")));
  }

  @Test
  void depositUseCaseShouldFailWhenClientHasNoAccount() {
    var clientService = new ClientService(provider);
    var accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    clientService.createClient(
        new CreateClientRequest("NoAcc", "noacc@test.com", "p", Provider.LOCAL, null, true, null));

    assertThrows(
        InvalidCredentialsException.class,
        () ->
            new DepositAccountUseCase(accountService, clientService)
                .execute(new DepositAccountCommand("noacc@test.com", "1.00")));
  }

  @Test
  void listTransfersUseCaseShouldEnforceOwnership() {
    var clientService = new ClientService(provider);
    var accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    var transactionService = new TransactionService(provider);

    long clientId = insertClient("Owner", "owner@test.com");
    long accountId = insertAccount(clientId, "600-000-002");

    var ownerAuth = new AuthService(provider, fixedClaims(clientId));
    var useCase = new ListTransfersByAccountUseCase(transactionService, accountService, ownerAuth);
    assertEquals(0, useCase.execute(accountId, "tok").size());

    var strangerAuth = new AuthService(provider, fixedClaims(clientId + 999));
    var strangerUseCase =
        new ListTransfersByAccountUseCase(transactionService, accountService, strangerAuth);
    assertThrows(
        UnauthorizedOperationException.class, () -> strangerUseCase.execute(accountId, "tok"));
  }

  @Test
  void listListingsByOwnerUseCaseShouldValidateToken() {
    var accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    var listingService = new ArtifactListingService(provider);
    var jwtService = TestJwt.create();

    long clientId = insertClient("Owner", "owner@test.com");
    insertAccount(clientId, "600-000-003");

    var useCase =
        new ListArtifactListingsByOwnerUseCase(accountService, listingService, jwtService);

    assertEquals(0, useCase.execute(jwtService.generateToken(clientId), 0, 10).items().size());

    assertThrows(UnauthorizedOperationException.class, () -> useCase.execute("garbage", 0, 10));

    assertThrows(
        InvalidCredentialsException.class,
        () -> useCase.execute(jwtService.generateToken(clientId + 999), 0, 10));
  }

  @Test
  void hikariConnectionProviderShouldServeConnections() throws Exception {
    var hikari =
        new HikariConnectionProvider(
            "jdbc:h2:mem:hikari_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");

    try (var conn = hikari.getConnection()) {
      assertTrue(conn.isValid(2));
    }
  }

  @Test
  void spyPrivateKeyStorageShouldRecordSaves() throws Exception {
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    byte[] encoded = generator.generateKeyPair().getPrivate().getEncoded();

    SpyPrivateKeyStorage spy = new SpyPrivateKeyStorage();
    assertFalse(spy.wasSaveCalled());

    spy.save(7L, encoded);
    assertTrue(spy.wasSaveCalled());
    assertEquals(7L, spy.getSavedAccountId());
    assertArrayEquals(encoded, spy.getSavedPrivateKey());
    assertNotNull(spy.get(7L));

    spy.delete(7L);
  }
}
