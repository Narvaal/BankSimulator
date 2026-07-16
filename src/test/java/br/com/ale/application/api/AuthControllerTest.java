package br.com.ale.application.api;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.application.account.usecase.ResendVerificationUseCase;
import br.com.ale.application.account.usecase.VerifyAccountUseCase;
import br.com.ale.application.auth.usecase.GoogleLoginUseCase;
import br.com.ale.application.auth.usecase.LocalLoginUseCase;
import br.com.ale.dto.AuthResponse;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateGoogleAuthenticationRequest;
import br.com.ale.dto.ResendVerificationRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.account.HashAccountNumberGenerator;
import br.com.ale.service.auth.GoogleTokenVerifier;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.email.EmailTemplateService;
import br.com.ale.service.email.EmailVerificationSender;
import br.com.ale.support.DbTestSupport;
import br.com.ale.support.RecordingEmailService;
import br.com.ale.support.TestJwt;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class AuthControllerTest extends DbTestSupport {

  private static final String GOOGLE_CLIENT_ID = "test-client-id";
  private static final String VALID_PASSWORD = "Str0ng!Pass";

  private static class FakeGoogleTokenVerifier extends GoogleTokenVerifier {
    Map<String, Object> payload;

    @Override
    public Map<String, Object> verify(String idToken) {
      return payload;
    }
  }

  private RecordingEmailService emailService;
  private FakeGoogleTokenVerifier verifier;
  private CreateAccountUseCase createAccountUseCase;
  private AuthController controller;

  @BeforeEach
  void setupController() {
    var clientService = new ClientService(provider);
    var keyStorage = new InMemoryPrivateKeyStorage();
    var accountService = new AccountService(provider, keyStorage);
    var emailVerificationService = new EmailVerificationService(provider);
    emailService = new RecordingEmailService();
    var sender = new EmailVerificationSender(emailService, new EmailTemplateService());
    var jwtService = TestJwt.create();
    verifier = new FakeGoogleTokenVerifier();

    createAccountUseCase =
        new CreateAccountUseCase(
            accountService,
            clientService,
            new HashAccountNumberGenerator(),
            emailVerificationService,
            sender,
            new KeyPairService(),
            keyStorage);

    var authCookieService = new AuthCookieService();
    ReflectionTestUtils.setField(authCookieService, "cookieDomain", "");
    ReflectionTestUtils.setField(authCookieService, "cookieSecure", false);
    ReflectionTestUtils.setField(authCookieService, "cookieSameSite", "Lax");

    controller =
        new AuthController(
            new LocalLoginUseCase(clientService, jwtService),
            new GoogleLoginUseCase(
                new HashAccountNumberGenerator(),
                accountService,
                clientService,
                jwtService,
                verifier,
                GOOGLE_CLIENT_ID,
                new KeyPairService(),
                keyStorage),
            authCookieService,
            new VerifyAccountUseCase(emailVerificationService, clientService, jwtService),
            new ResendVerificationUseCase(clientService, emailVerificationService, sender));
  }

  private String registerAndGetToken() {
    createAccountUseCase.execute(new CreateAccountCommand("John", "john@test.com", VALID_PASSWORD));
    String html = emailService.sent.get(emailService.sent.size() - 1).html();
    int idx = html.indexOf("token=");
    return html.substring(idx + 6, html.indexOf('"', idx));
  }

  @Test
  void verifyEmailShouldSetCookieAndRedirect() throws Exception {
    String token = registerAndGetToken();
    MockHttpServletResponse response = new MockHttpServletResponse();

    controller.verifyEmail(token, response);

    assertTrue(response.getHeader(HttpHeaders.SET_COOKIE).contains("AUTH_TOKEN="));
    assertEquals("https://app.alessandro-bezerra.me/inventory", response.getRedirectedUrl());
  }

  @Test
  void sessionShouldReturnTokenFromCookieOr401() {
    MockHttpServletRequest withCookie = new MockHttpServletRequest();
    withCookie.setCookies(new Cookie("AUTH_TOKEN", "jwt"));
    ResponseEntity<?> ok = controller.session(withCookie);
    assertEquals(200, ok.getStatusCode().value());

    ResponseEntity<?> unauthorized = controller.session(new MockHttpServletRequest());
    assertEquals(401, unauthorized.getStatusCode().value());
  }

  @Test
  void loginShouldAuthenticateVerifiedUser() throws Exception {
    String token = registerAndGetToken();
    controller.verifyEmail(token, new MockHttpServletResponse());

    MockHttpServletResponse response = new MockHttpServletResponse();
    ResponseEntity<?> result =
        controller.login(
            new CreateAuthenticationRequest("john@test.com", VALID_PASSWORD), response);

    assertEquals(200, result.getStatusCode().value());
    assertTrue(response.getHeader(HttpHeaders.SET_COOKIE).contains("AUTH_TOKEN="));
  }

  @Test
  void loginShouldFlagUnverifiedEmailAndBadCredentials() {
    registerAndGetToken();

    ResponseEntity<?> unverified =
        controller.login(
            new CreateAuthenticationRequest("john@test.com", VALID_PASSWORD),
            new MockHttpServletResponse());
    assertEquals(400, unverified.getStatusCode().value());
    assertTrue(unverified.getBody().toString().contains("EMAIL_NOT_VERIFIED"));

    ResponseEntity<?> wrongPassword =
        controller.login(
            new CreateAuthenticationRequest("john@test.com", "Wr0ng!Pass"),
            new MockHttpServletResponse());
    assertEquals(400, wrongPassword.getStatusCode().value());
    assertTrue(wrongPassword.getBody().toString().contains("INVALID_CREDENTIALS"));
  }

  @Test
  void resendShouldSendVerificationEmailAgain() {
    registerAndGetToken();
    int before = emailService.sent.size();

    controller.resend(new ResendVerificationRequest("john@test.com"));

    assertEquals(before + 1, emailService.sent.size());
  }

  @Test
  void googleLoginShouldAuthenticateAndSetCookie() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("sub", "google-1");
    payload.put("email", "guser@test.com");
    payload.put("name", "Google User");
    payload.put("picture", null);
    payload.put("email_verified", "true");
    payload.put("aud", GOOGLE_CLIENT_ID);
    verifier.payload = payload;

    MockHttpServletResponse response = new MockHttpServletResponse();
    AuthResponse auth =
        controller.login(new CreateGoogleAuthenticationRequest("id-token"), response);

    assertEquals("Authenticated", auth.name());
    assertTrue(response.getHeader(HttpHeaders.SET_COOKIE).contains("AUTH_TOKEN="));
  }
}
