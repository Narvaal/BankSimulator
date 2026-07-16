package br.com.ale.application.auth.usecase;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.auth.command.GoogleLoginCommand;
import br.com.ale.application.auth.command.LocalLoginCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.account.HashAccountNumberGenerator;
import br.com.ale.service.auth.GoogleTokenVerifier;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.TestJwt;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUseCasesTest extends DbTestSupport {

  private static final String GOOGLE_CLIENT_ID = "test-client-id.apps.googleusercontent.com";

  private static class FakeGoogleTokenVerifier extends GoogleTokenVerifier {
    Map<String, Object> payload;

    @Override
    public Map<String, Object> verify(String idToken) {
      return payload;
    }
  }

  private ClientService clientService;
  private AccountService accountService;
  private FakeGoogleTokenVerifier verifier;
  private LocalLoginUseCase localLogin;
  private GoogleLoginUseCase googleLogin;

  @BeforeEach
  void setupUseCases() {
    clientService = new ClientService(provider);
    accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
    verifier = new FakeGoogleTokenVerifier();

    localLogin = new LocalLoginUseCase(clientService, TestJwt.create());
    googleLogin =
        new GoogleLoginUseCase(
            new HashAccountNumberGenerator(),
            accountService,
            clientService,
            TestJwt.create(),
            verifier,
            GOOGLE_CLIENT_ID,
            new KeyPairService(),
            new InMemoryPrivateKeyStorage());
  }

  private void createLocalClient(String email, String password, boolean verified) {
    clientService.createClient(
        new CreateClientRequest(
            "John", email, PasswordHasher.hash(password), Provider.LOCAL, null, verified, null));
  }

  private Map<String, Object> googlePayload() {
    Map<String, Object> payload = new HashMap<>();
    payload.put("sub", "google-123");
    payload.put("email", "guser@test.com");
    payload.put("name", "Google User");
    payload.put("picture", "https://pic");
    payload.put("email_verified", "true");
    payload.put("aud", GOOGLE_CLIENT_ID);
    return payload;
  }

  @Test
  void localLoginShouldIssueJwtForVerifiedClient() {
    createLocalClient("john@test.com", "Str0ng!Pass", true);

    AuthToken token = localLogin.execute(new LocalLoginCommand("john@test.com", "Str0ng!Pass"));
    assertNotNull(token.getToken());
  }

  @Test
  void localLoginShouldRejectBadCredentialsAndUnverifiedEmail() {
    createLocalClient("john@test.com", "Str0ng!Pass", false);

    IllegalArgumentException unverified =
        assertThrows(
            IllegalArgumentException.class,
            () -> localLogin.execute(new LocalLoginCommand("john@test.com", "Str0ng!Pass")));
    assertEquals("Email not verified", unverified.getMessage());

    assertThrows(
        IllegalArgumentException.class,
        () -> localLogin.execute(new LocalLoginCommand("john@test.com", "wrong")));
    assertThrows(
        IllegalArgumentException.class,
        () -> localLogin.execute(new LocalLoginCommand("ghost@test.com", "Str0ng!Pass")));
  }

  @Test
  void googleLoginShouldCreateClientAndAccountOnFirstLogin() {
    verifier.payload = googlePayload();

    AuthToken token = googleLogin.execute(new GoogleLoginCommand("id-token"));
    assertNotNull(token.getToken());

    var client =
        clientService.getClientByProviderAndId(Provider.GOOGLE, "google-123").orElseThrow();
    assertEquals("Google User", client.getName());
    assertTrue(accountService.getAccountByClientId(client.getId()).isPresent());
  }

  @Test
  void googleLoginShouldReuseExistingClientAndAccount() {
    verifier.payload = googlePayload();

    long firstClientId = googleLogin.execute(new GoogleLoginCommand("id-token")).getClientId();
    long secondClientId = googleLogin.execute(new GoogleLoginCommand("id-token")).getClientId();

    assertEquals(firstClientId, secondClientId);
  }

  @Test
  void googleLoginShouldRejectInvalidPayloads() {
    verifier.payload = null;
    assertThrows(
        SecurityException.class, () -> googleLogin.execute(new GoogleLoginCommand("id-token")));

    verifier.payload = Map.of();
    assertThrows(
        SecurityException.class, () -> googleLogin.execute(new GoogleLoginCommand("id-token")));
  }

  @Test
  void googleLoginShouldRejectUnverifiedEmailAndWrongAudience() {
    verifier.payload = googlePayload();
    verifier.payload.put("email_verified", "false");
    assertThrows(
        SecurityException.class, () -> googleLogin.execute(new GoogleLoginCommand("id-token")));

    verifier.payload = googlePayload();
    verifier.payload.put("aud", "other-audience");
    SecurityException ex =
        assertThrows(
            SecurityException.class, () -> googleLogin.execute(new GoogleLoginCommand("id-token")));
    assertTrue(ex.getMessage().contains("audience"));
  }
}
