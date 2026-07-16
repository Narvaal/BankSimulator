package br.com.ale.infrastructure.auth;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AuthCookieServiceTest {

  private AuthCookieService service;

  @BeforeEach
  void setup() {
    service = new AuthCookieService();
    ReflectionTestUtils.setField(service, "cookieDomain", "");
    ReflectionTestUtils.setField(service, "cookieSecure", false);
    ReflectionTestUtils.setField(service, "cookieSameSite", "Lax");
  }

  @Test
  void addAuthCookieShouldSetHttpOnlyCookieHeader() {
    MockHttpServletResponse response = new MockHttpServletResponse();

    service.addAuthCookie(response, "jwt-token");

    String header = response.getHeader(HttpHeaders.SET_COOKIE);
    assertNotNull(header);
    assertTrue(header.contains("AUTH_TOKEN=jwt-token"));
    assertTrue(header.contains("HttpOnly"));
    assertTrue(header.contains("SameSite=Lax"));
    assertFalse(header.contains("Domain="));
  }

  @Test
  void addAuthCookieShouldIncludeDomainAndSecureWhenConfigured() {
    ReflectionTestUtils.setField(service, "cookieDomain", ".example.com");
    ReflectionTestUtils.setField(service, "cookieSecure", true);
    MockHttpServletResponse response = new MockHttpServletResponse();

    service.addAuthCookie(response, "jwt-token");

    String header = response.getHeader(HttpHeaders.SET_COOKIE);
    assertTrue(header.contains("Domain=example.com") || header.contains("Domain=.example.com"));
    assertTrue(header.contains("Secure"));
  }

  @Test
  void extractTokenShouldPreferAuthorizationHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
    request.setCookies(new Cookie("AUTH_TOKEN", "cookie-token"));

    assertEquals("header-token", service.extractToken(request));
  }

  @Test
  void extractTokenShouldFallBackToCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("AUTH_TOKEN", "cookie-token"));

    assertEquals("cookie-token", service.extractToken(request));
  }

  @Test
  void extractTokenShouldThrow401WhenAbsent() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    assertThrows(ResponseStatusException.class, () -> service.extractToken(request));
  }

  @Test
  void extractTokenOrNullShouldHandleMissingBlankAndForeignCookies() {
    assertNull(service.extractTokenOrNull(new MockHttpServletRequest()));

    MockHttpServletRequest blank = new MockHttpServletRequest();
    blank.setCookies(new Cookie("AUTH_TOKEN", ""));
    assertNull(service.extractTokenOrNull(blank));

    MockHttpServletRequest foreign = new MockHttpServletRequest();
    foreign.setCookies(new Cookie("OTHER", "x"));
    assertNull(service.extractTokenOrNull(foreign));
  }
}
