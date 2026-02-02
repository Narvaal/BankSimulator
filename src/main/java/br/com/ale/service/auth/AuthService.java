package br.com.ale.service.auth;

import br.com.ale.dao.ClientDAO;
import br.com.ale.dao.CredentialDAO;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.Credential;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateCredentialRequest;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.auth.TokenGenerator;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class AuthService {

    private final ConnectionProvider connectionProvider;
    private final CredentialDAO credentialDAO = new CredentialDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private TokenGenerator tokenGenerator;
    private final PlainTextPasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    public AuthService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public AuthService(
            ConnectionProvider connectionProvider,
            TokenGenerator tokenGenerator
    ) {
        this.connectionProvider = connectionProvider;
        this.tokenGenerator = tokenGenerator;
    }

    public AuthToken authenticate(CreateAuthenticationRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            if (tokenGenerator == null) {
                throw new IllegalStateException("Token generator not configured");
            }

            Credential credential =
                    credentialDAO.selectByEmail(conn, request.email())
                            .orElseThrow(() ->
                                    new InvalidCredentialsException(
                                            "Invalid credentials"
                                    )
                            );

            if (!passwordEncoder.matches(
                    request.password(),
                    credential.getPasswordHash())
            ) {
                throw new InvalidCredentialsException(
                        "Invalid credentials"
                );
            }

            return tokenGenerator.generate(credential.getClientId());

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while authentication client " +
                            "[email=" + request.email() + "]",
                    e
            );
        }
    }

    public long register(CreateCredentialRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            Client client = clientDAO.selectByEmail(conn, request.email())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found [email=" + request.email() + "]"
                            )
                    );

            String hashPassword = passwordEncoder.encode(request.password());

            long credentialId =
                    credentialDAO.insert(
                            conn,
                            client.getId(),
                            client.getEmail(),
                            hashPassword
                    );

            return credentialId;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while registering client " +
                            "[email=" + request.email() + "]",
                    e
            );
        }
    }

    public TokenClaims validateToken(String token) {
        if (tokenGenerator == null) {
            throw new IllegalStateException("Token generator not configured");
        }
        return tokenGenerator.validate(token);
    }
}
