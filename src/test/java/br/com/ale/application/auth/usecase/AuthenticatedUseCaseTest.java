package br.com.ale.application.auth.usecase;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.auth.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticatedUseCaseTest {

    private TokenGenerator tokenGenerator;
    private AuthenticatedUseCase useCase;

    @BeforeEach
    void setup() {
        KeyPair keyPair = generateKeyPair();

        tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        useCase = new AuthenticatedUseCase(tokenGenerator);
    }

    @Test
    void shouldValidateTokenSuccessfully() {

        AuthToken token =
                tokenGenerator.generate(10L);

        TokenClaims claims =
                useCase.execute(token.getToken());

        assertEquals(10L, claims.clientId());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute("invalid.token.value")
        );
    }

    @Test
    void shouldFailWhenTokenIsExpired() {

        String payload =
                "10:" +
                        Instant.now().minusSeconds(120).toEpochMilli() + ":" +
                        Instant.now().minusSeconds(60).toEpochMilli() + ":" +
                        "token-id";

        String signature =
                ((SimpleTokenGenerator) tokenGenerator)
                        .signForTest(payload);

        String token =
                java.util.Base64.getEncoder()
                        .encodeToString(payload.getBytes())
                        + "." + signature;

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(token)
        );
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);
            return generator.generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
