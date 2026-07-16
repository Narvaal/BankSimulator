package br.com.ale.support;

import br.com.ale.service.auth.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.test.util.ReflectionTestUtils;

/** JwtService pronto para testes (secret fixo, expiração de 1h). */
public final class TestJwt {

  private TestJwt() {}

  public static JwtService create() {
    JwtService service = new JwtService();
    String secret =
        Base64.getEncoder()
            .encodeToString("test-secret-key-for-unit-tests!!".getBytes(StandardCharsets.UTF_8));
    ReflectionTestUtils.setField(service, "secretKey", secret);
    ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);
    return service;
  }
}
