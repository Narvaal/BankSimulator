package br.com.ale.service.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.CreateCredentialRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private ClientService clientService;
    private TestConnectionProvider provider;
    private AuthService authService;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        authService = new AuthService(provider);
        clientService = new ClientService(provider);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldRegisterCredentialSuccessfully() {

        Client client = clientService.createClient(validClient());
        String password = "password123";

        CreateCredentialRequest request =
                new CreateCredentialRequest(
                        client.getDocument(),
                        password
                );

        long credentialId =
                assertDoesNotThrow(() ->
                        authService.register(request)
                );

        assertTrue(credentialId > 0);
    }

    @Test
    void shouldAuthenticateSuccessfully() {

        Client client = clientService.createClient(validClient());
        String password = "password123";

        authService.register(
                new CreateCredentialRequest(
                        client.getDocument(),
                        password
                )
        );

        AuthToken token =
                assertDoesNotThrow(() ->
                        authService.authenticate(
                                new CreateAuthenticationRequest(
                                        client.getDocument(),
                                        password
                                )
                        )
                );

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(client.getId(), token.getClientID());
    }

    @Test
    void shouldFailWhenDocumentDoesNotExist() {

        CreateAuthenticationRequest request =
                new CreateAuthenticationRequest(
                        "99999999999",
                        "any-password"
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(request)
        );
    }

    @Test
    void shouldFailWhenPasswordIsInvalid() {

        Client client = clientService.createClient(validClient());

        authService.register(
                new CreateCredentialRequest(
                        client.getDocument(),
                        "correct-password"
                )
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.authenticate(
                        new CreateAuthenticationRequest(
                                client.getDocument(),
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
}

