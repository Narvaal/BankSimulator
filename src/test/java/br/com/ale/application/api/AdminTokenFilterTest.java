package br.com.ale.application.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminTokenFilterTest {

  private MockHttpServletResponse run(
      AdminTokenFilter filter, String method, String uri, String token) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
    request.setRequestURI(uri);
    if (token != null) {
      request.addHeader("X-Admin-Token", token);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, new MockFilterChain());
    return response;
  }

  @Test
  void shouldLetNonAdminRoutesThrough() throws Exception {
    AdminTokenFilter filter = new AdminTokenFilter("secret");

    assertEquals(200, run(filter, "GET", "/artifacts/bundles", null).getStatus());
    assertEquals(200, run(filter, "GET", "/market", null).getStatus());
  }

  @Test
  void shouldBlockAdminRoutesWithoutToken() throws Exception {
    AdminTokenFilter filter = new AdminTokenFilter("secret");

    assertEquals(403, run(filter, "POST", "/admin/accounts/deposit", null).getStatus());
    assertEquals(403, run(filter, "POST", "/artifacts/bundles", null).getStatus());
  }

  @Test
  void shouldBlockWrongTokenAndAcceptCorrectOne() throws Exception {
    AdminTokenFilter filter = new AdminTokenFilter("secret");

    assertEquals(403, run(filter, "POST", "/admin/accounts/deposit", "wrong").getStatus());
    assertEquals(200, run(filter, "POST", "/admin/accounts/deposit", "secret").getStatus());
    assertEquals(200, run(filter, "POST", "/artifacts/bundles", "secret").getStatus());
  }

  @Test
  void shouldBlockAdminRoutesWhenTokenNotConfigured() throws Exception {
    AdminTokenFilter filter = new AdminTokenFilter("");

    assertEquals(403, run(filter, "POST", "/admin/accounts/deposit", "anything").getStatus());
  }
}
