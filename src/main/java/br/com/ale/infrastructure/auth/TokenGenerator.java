package br.com.ale.infrastructure.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface TokenGenerator {

    AuthToken generate(long clientId);

    TokenClaims validate(String token);
}
