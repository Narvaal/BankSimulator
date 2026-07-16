package br.com.ale.service.auth;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleTokenVerifier {

  private static final String GOOGLE_TOKEN_INFO =
      "https://oauth2.googleapis.com/tokeninfo?id_token=";

  public Map<String, Object> verify(String idToken) {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(GOOGLE_TOKEN_INFO + idToken, Map.class);
  }
}
