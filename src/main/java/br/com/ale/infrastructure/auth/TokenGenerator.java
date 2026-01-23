package br.com.ale.infrastructure.auth;

import br.com.ale.domain.auth.AuthToken;

public interface TokenGenerator {

    AuthToken generate(long clientId);
}
