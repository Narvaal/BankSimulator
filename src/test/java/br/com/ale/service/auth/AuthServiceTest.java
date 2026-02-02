package br.com.ale.service.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.CreateCredentialRequest;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private ClientService clientService;
    private TestConnectionProvider provider;
    private AuthService authService;
    private SimpleTokenGenerator simpleTokenGenerator;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();

        KeyPair keyPair = generateKeyPair();

        simpleTokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService =
                new AuthService(
                        provider,
                        simpleTokenGenerator
                );

        clientService = new ClientService(provider);

        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset_price_history");
            stmt.execute("DELETE FROM asset_transfer");
            stmt.execute("DELETE FROM asset_listing");
            stmt.execute("DELETE FROM asset_unit");
            stmt.execute("DELETE FROM asset");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldRegisterCredentialSuccessfully() {

        Client client = clientService.createClient(validClient());

        long credentialId =
                authService.register(
                        new CreateCredentialRequest(
                                client.getEmail(),
                                "password123"
                        )
                );

        assertTrue(credentialId > 0);
    }

    @Test
    void shouldAuthenticateAndValidateTokenSuccessfully() {

        Client client = clientService.createClient(validClient());
        String password = "password123";

        authService.register(
                new CreateCredentialRequest(
                        client.getEmail(),
                        password
                )
        );

        AuthToken token =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                client.getEmail(),
                                password
                        )
                );

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(client.getId(), token.getClientId());

        var claims =
                simpleTokenGenerator.validate(token.getToken());

        assertEquals(client.getId(), claims.clientId());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldFailWhenEmailDoesNotExist() {

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(
                        new CreateAuthenticationRequest(
                                "99999999999",
                                "any-password"
                        )
                )
        );
    }

    @Test
    void shouldFailWhenPasswordIsInvalid() {

        Client client = clientService.createClient(validClient());

        authService.register(
                new CreateCredentialRequest(
                        client.getEmail(),
                        "correct-password"
                )
        );

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

    private CreateClientRequest validClient() {
        return new CreateClientRequest(
                "John Doe",
                "123456789"
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
