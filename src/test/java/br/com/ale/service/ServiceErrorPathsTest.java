package br.com.ale.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactBundleService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactTransferService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Todo service deve embrulhar falhas de conexão em RuntimeException. */
class ServiceErrorPathsTest {

  private final ConnectionProvider broken =
      () -> {
        throw new RuntimeException("db down");
      };

  @Test
  void accountServiceShouldWrapConnectionFailures() {
    AccountService service = new AccountService(broken, new InMemoryPrivateKeyStorage());

    assertThrows(
        RuntimeException.class,
        () ->
            service.createAccount(
                new CreateAccountRequest(1L, "1", AccountType.DEFAULT, AccountStatus.ACTIVE)));
    assertThrows(RuntimeException.class, () -> service.getAccountByNumber("1"));
    assertThrows(RuntimeException.class, () -> service.getAccountById(1L));
    assertThrows(RuntimeException.class, () -> service.getAccountByClientId(1L));
    assertThrows(RuntimeException.class, () -> service.getAccountDetailsById(1L));
    assertThrows(RuntimeException.class, () -> service.tryClaimArtifactUnit("1"));
    assertThrows(RuntimeException.class, () -> service.selectNextClaimById("1"));
    assertThrows(
        RuntimeException.class,
        () ->
            service.updateAccount(
                new UpdateAccountRequest(1L, "1", AccountType.DEFAULT, AccountStatus.ACTIVE)));
    assertThrows(RuntimeException.class, () -> service.transfer(1L, 2L, BigDecimal.TEN));
    assertThrows(RuntimeException.class, () -> service.credit("1", BigDecimal.TEN));
    assertThrows(RuntimeException.class, () -> service.debit("1", BigDecimal.TEN));
    assertThrows(RuntimeException.class, () -> service.getPublicProfile(1L));
    assertThrows(RuntimeException.class, () -> service.searchByName("q", 0, 10));
  }

  @Test
  void clientServiceShouldWrapConnectionFailures() {
    ClientService service = new ClientService(broken);

    assertThrows(
        RuntimeException.class,
        () ->
            service.createClient(
                new CreateClientRequest("n", "e", "p", Provider.LOCAL, null, false, null)));
    assertThrows(
        RuntimeException.class, () -> service.updateClient(new UpdateClientRequest(1L, "p")));
    assertThrows(RuntimeException.class, () -> service.getClientByEmail("e"));
    assertThrows(
        RuntimeException.class, () -> service.getClientByProviderAndId(Provider.LOCAL, "x"));
    assertThrows(RuntimeException.class, () -> service.getClientById(1L));
    assertThrows(RuntimeException.class, () -> service.activate(1L));
    assertThrows(RuntimeException.class, () -> service.deleteClient(1L));
  }

  @Test
  void transactionAndEmailServicesShouldWrapConnectionFailures() {
    TransactionService transactionService = new TransactionService(broken);
    assertThrows(
        RuntimeException.class,
        () ->
            transactionService.createTransaction(
                new CreateTransactionRequest(
                    1L,
                    "1",
                    2L,
                    "2",
                    BigDecimal.TEN,
                    TransactionType.TRANSFERENCE,
                    TransactionStatus.PENDING,
                    "s")));
    assertThrows(
        RuntimeException.class,
        () ->
            transactionService.updateStatus(
                new UpdateTransactionRequest(1L, TransactionStatus.COMPLETE)));
    assertThrows(RuntimeException.class, () -> transactionService.listTransfersByAccount(1L));

    EmailVerificationService emailService = new EmailVerificationService(broken);
    assertThrows(
        RuntimeException.class,
        () ->
            emailService.create(
                new CreateEmailVerificationRequest(
                    1L, "t", EmailVerificationType.EMAIL_VERIFICATION, Instant.now(), null)));
    assertThrows(
        RuntimeException.class,
        () -> emailService.findByToken("t", EmailVerificationType.EMAIL_VERIFICATION));
    assertThrows(
        RuntimeException.class,
        () -> emailService.findActiveByClientId(1L, EmailVerificationType.EMAIL_VERIFICATION));
    assertThrows(
        RuntimeException.class,
        () -> emailService.confirmToken("t", EmailVerificationType.EMAIL_VERIFICATION));
  }

  @Test
  void artifactServicesShouldWrapConnectionFailures() {
    ArtifactService artifactService = new ArtifactService(broken);
    assertThrows(
        RuntimeException.class,
        () -> artifactService.createAsset(new CreateArtifactRequest(Map.of(), 1)));
    assertThrows(RuntimeException.class, () -> artifactService.selectById(1L));
    assertThrows(RuntimeException.class, () -> artifactService.listArtifacts());

    ArtifactUnitService unitService =
        new ArtifactUnitService(broken, new ArtifactWebhookNotifier("", false));
    assertThrows(
        RuntimeException.class,
        () -> unitService.createArtifactUnit(new CreateArtifactUnitRequest(1L, 1L)));
    assertThrows(RuntimeException.class, () -> unitService.selectById(1L));
    assertThrows(RuntimeException.class, () -> unitService.tryUpdateToMarket(1L, 1L));
    assertThrows(RuntimeException.class, () -> unitService.selectByOwnerAccount(1L, 0, 10));

    ArtifactListingService listingService = new ArtifactListingService(broken);
    assertThrows(
        RuntimeException.class,
        () -> listingService.changePrice(1L, BigDecimal.TEN, 1L, ReasonType.UPDATED));
    assertThrows(RuntimeException.class, () -> listingService.cancelListing(1L, 1L));
    assertThrows(
        RuntimeException.class,
        () ->
            listingService.createArtifactOffer(
                new CreateArtifactListingRequest(
                    1L, 1L, BigDecimal.TEN, ArtifactListingStatus.ACTIVE)));
    assertThrows(RuntimeException.class, () -> listingService.selectById(1L));
    assertThrows(
        RuntimeException.class, () -> listingService.selectByStatus(ArtifactListingStatus.ACTIVE));
    assertThrows(
        RuntimeException.class, () -> listingService.updateStatus(1L, ArtifactListingStatus.SOLD));
    assertThrows(RuntimeException.class, () -> listingService.selectByArtifactUnitId(1L));
    assertThrows(
        RuntimeException.class,
        () -> listingService.selectActiveByActiveStatus(1L, ArtifactListingFilter.empty(), 0, 10));
    assertThrows(RuntimeException.class, () -> listingService.selectByOwnerAccount(1L, 0, 10));

    ArtifactBundleService bundleService = new ArtifactBundleService(broken);
    assertThrows(RuntimeException.class, () -> bundleService.createBundle(List.of(), "weekly-1"));
    assertThrows(RuntimeException.class, () -> bundleService.listBundles(0, 10));
    assertThrows(RuntimeException.class, () -> bundleService.listBundleItems(1L, 0, 10));

    ArtifactTransferService transferService =
        new ArtifactTransferService(broken, new ArtifactWebhookNotifier("", false));
    assertThrows(RuntimeException.class, () -> transferService.publicFeed(null, 0, 10));
    assertThrows(RuntimeException.class, () -> transferService.selectByUnitId(1L));
    assertThrows(RuntimeException.class, () -> transferService.selectById(1L));
  }

  @Test
  void authServiceShouldWrapFailuresAndRequireTokenGenerator() {
    AuthService withoutGenerator = new AuthService(broken);
    assertThrows(IllegalStateException.class, () -> withoutGenerator.validateToken("t"));

    AuthService service =
        new AuthService(
            broken,
            new br.com.ale.infrastructure.auth.TokenGenerator() {
              @Override
              public br.com.ale.domain.auth.AuthToken generate(long clientId) {
                return new br.com.ale.domain.auth.AuthToken(clientId, "tok", Instant.now());
              }

              @Override
              public br.com.ale.domain.auth.TokenClaims validate(String token) {
                return new br.com.ale.domain.auth.TokenClaims(1L, Instant.now());
              }
            });
    assertThrows(
        RuntimeException.class,
        () -> service.authenticate(new CreateAuthenticationRequest("e", "p")));
  }
}
