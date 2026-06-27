package br.com.ale.service.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.crypto.KeyPairService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private ClientService clientService;
    private TestConnectionProvider provider;
    private AuthService authService;
    private SimpleTokenGenerator tokenGenerator;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();

        KeyPair keyPair = new KeyPairService().generate();
        tokenGenerator = new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic());

        authService = new AuthService(provider, tokenGenerator);
        clientService = new ClientService(provider);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM artifact_price_history");
            stmt.execute("DELETE FROM artifact_transfer");
            stmt.execute("DELETE FROM artifact_listing");
            stmt.execute("DELETE FROM artifact_unit");
            stmt.execute("DELETE FROM artifact");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldAuthenticateSuccessfully() {

        String rawPassword = "password123";
        Client client = clientService.createClient(new CreateClientRequest(
                "John Doe",
                "john@test.com",
                PasswordHasher.hash(rawPassword),
                Provider.LOCAL,
                null,
                false,
                null
        ));

        AuthToken token = authService.authenticate(
                new CreateAuthenticationRequest(client.getEmail(), rawPassword)
        );

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(client.getId(), token.getClientId());
    }

    @Test
    void shouldReturnValidJwtAfterAuthentication() {

        String rawPassword = "password123";
        Client client = clientService.createClient(new CreateClientRequest(
                "John Doe",
                "john@test.com",
                PasswordHasher.hash(rawPassword),
                Provider.LOCAL,
                null,
                false,
                null
        ));

        AuthToken token = authService.authenticate(
                new CreateAuthenticationRequest(client.getEmail(), rawPassword)
        );

        TokenClaims claims = tokenGenerator.validate(token.getToken());
        assertEquals(client.getId(), claims.clientId());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldFailWhenEmailDoesNotExist() {

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(
                        new CreateAuthenticationRequest(
                                "nonexistent@test.com",
                                "any-password"
                        )
                )
        );
    }

    @Test
    void shouldFailWhenPasswordIsInvalid() {

        String rawPassword = "correct-password";
        Client client = clientService.createClient(new CreateClientRequest(
                "John Doe",
                "john@test.com",
                PasswordHasher.hash(rawPassword),
                Provider.LOCAL,
                null,
                false,
                null
        ));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(
                        new CreateAuthenticationRequest(
                                client.getEmail(),
                                "wrong-password"
                        )
                )
        );
    }
}
