package br.com.ale.application.auth.usecase;

import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.infrastructure.auth.TokenGenerator;

public class AuthenticatedUseCase {

    private final TokenGenerator tokenGenerator;

    public AuthenticatedUseCase(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public TokenClaims execute(String token) {
        return tokenGenerator.validate(token);
    }
}
