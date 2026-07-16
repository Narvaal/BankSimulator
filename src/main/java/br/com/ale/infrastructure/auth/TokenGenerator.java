package br.com.ale.infrastructure.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;

public interface TokenGenerator {

  AuthToken generate(long clientId);

  TokenClaims validate(String token);
}
