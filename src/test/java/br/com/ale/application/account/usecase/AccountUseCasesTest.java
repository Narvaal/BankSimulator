package br.com.ale.application.account.usecase;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.account.command.ChangePasswordCommand;
import br.com.ale.application.account.command.ChangePasswordSenderCommand;
import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.application.account.command.ResendVerificationCommand;
import br.com.ale.application.account.command.VerifyAccountCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.account.HashAccountNumberGenerator;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.email.EmailTemplateService;
import br.com.ale.service.email.EmailVerificationSender;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.RecordingEmailService;
import br.com.ale.support.TestJwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountUseCasesTest extends DbTestSupport {

  private static final String VALID_PASSWORD = "Str0ng!Pass";

  private ClientService clientService;
  private AccountService accountService;
  private EmailVerificationService emailVerificationService;
  private RecordingEmailService emailService;
  private EmailVerificationSender sender;
  private InMemoryPrivateKeyStorage keyStorage;

  private CreateAccountUseCase createAccountUseCase;
  private VerifyAccountUseCase verifyAccountUseCase;
  private ResendVerificationUseCase resendVerificationUseCase;
  private RequestPasswordResetUseCase requestPasswordResetUseCase;
  private ChangePasswordUseCase changePasswordUseCase;

  @BeforeEach
  void setupUseCases() {
    clientService = new ClientService(provider);
    keyStorage = new InMemoryPrivateKeyStorage();
    accountService = new AccountService(provider, keyStorage);
    emailVerificationService = new EmailVerificationService(provider);
    emailService = new RecordingEmailService();
    sender = new EmailVerificationSender(emailService, new EmailTemplateService());

    createAccountUseCase =
        new CreateAccountUseCase(
            accountService,
            clientService,
            new HashAccountNumberGenerator(),
            emailVerificationService,
            sender,
            new KeyPairService(),
            keyStorage);
    verifyAccountUseCase =
        new VerifyAccountUseCase(emailVerificationService, clientService, TestJwt.create());
    resendVerificationUseCase =
        new ResendVerificationUseCase(clientService, emailVerificationService, sender);
    requestPasswordResetUseCase =
        new RequestPasswordResetUseCase(clientService, emailVerificationService, sender);
    changePasswordUseCase = new ChangePasswordUseCase(clientService, emailVerificationService);
  }

  private String tokenFromLastEmail() {
    String html = emailService.sent.get(emailService.sent.size() - 1).html();
    int idx = html.indexOf("token=");
    return html.substring(idx + 6, html.indexOf('"', idx));
  }

  @Test
  void createAccountShouldCreateClientAccountTokenAndKeys() {
    createAccountUseCase.execute(
        new CreateAccountCommand("John Doe", "john@test.com", VALID_PASSWORD));

    Client client = clientService.getClientByEmail("john@test.com");
    assertFalse(client.isEmailVerified());

    var account = accountService.getAccountByClientId(client.getId()).orElseThrow();
    assertNotNull(keyStorage.get(account.getId()));

    assertEquals(1, emailService.sent.size());
    assertTrue(
        emailVerificationService
            .findActiveByClientId(client.getId(), EmailVerificationType.EMAIL_VERIFICATION)
            .isPresent());
  }

  @Test
  void createAccountShouldRejectWeakPassword() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            createAccountUseCase.execute(
                new CreateAccountCommand("John", "john@test.com", "weak")));
    assertTrue(emailService.sent.isEmpty());
  }

  @Test
  void createAccountShouldRollbackClientWhenEmailFails() {
    emailService.fail = true;

    assertThrows(
        RuntimeException.class,
        () ->
            createAccountUseCase.execute(
                new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD)));

    assertThrows(RuntimeException.class, () -> clientService.getClientByEmail("john@test.com"));
  }

  @Test
  void verifyAccountShouldActivateClientAndIssueJwt() {
    createAccountUseCase.execute(new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD));
    String token = tokenFromLastEmail();

    AuthToken auth = verifyAccountUseCase.execute(new VerifyAccountCommand(token));

    assertNotNull(auth.getToken());
    assertTrue(clientService.getClientByEmail("john@test.com").isEmailVerified());

    assertThrows(
        RuntimeException.class,
        () -> verifyAccountUseCase.execute(new VerifyAccountCommand(token)));
  }

  @Test
  void resendVerificationShouldReuseActiveToken() {
    createAccountUseCase.execute(new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD));
    String originalToken = tokenFromLastEmail();

    resendVerificationUseCase.execute(new ResendVerificationCommand("john@test.com"));

    assertEquals(2, emailService.sent.size());
    assertEquals(originalToken, tokenFromLastEmail());
  }

  @Test
  void resendVerificationShouldDoNothingForVerifiedOrUnknownClients() {
    createAccountUseCase.execute(new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD));
    verifyAccountUseCase.execute(new VerifyAccountCommand(tokenFromLastEmail()));
    int sentBefore = emailService.sent.size();

    resendVerificationUseCase.execute(new ResendVerificationCommand("john@test.com"));
    resendVerificationUseCase.execute(new ResendVerificationCommand("ghost@test.com"));

    assertEquals(sentBefore, emailService.sent.size());
  }

  @Test
  void passwordResetFlowShouldChangePassword() {
    createAccountUseCase.execute(new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD));

    requestPasswordResetUseCase.execute(new ChangePasswordSenderCommand("john@test.com"));
    assertEquals("Reset your password", emailService.sent.get(1).subject());
    String resetToken = tokenFromLastEmail();

    changePasswordUseCase.execute(new ChangePasswordCommand("N3w!Password", resetToken));

    Client client = clientService.getClientByEmail("john@test.com");
    assertTrue(PasswordHasher.matches("N3w!Password", client.getPassword()));

    assertThrows(
        RuntimeException.class,
        () -> changePasswordUseCase.execute(new ChangePasswordCommand("0ther!Pass", resetToken)));
  }

  @Test
  void passwordResetShouldFailForUnknownEmail() {
    assertThrows(
        RuntimeException.class,
        () ->
            requestPasswordResetUseCase.execute(new ChangePasswordSenderCommand("ghost@test.com")));
  }
}
