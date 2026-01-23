package br.com.ale.infrastructure.auth;

import br.com.ale.domain.auth.AuthToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SimpleTokenGenerator implements TokenGenerator {

    @Override
    public AuthToken generate(long clientId) {

        return new AuthToken(
                clientId,
                UUID.randomUUID().toString(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
    }
}
