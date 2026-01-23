package br.com.ale.service.auth;

import br.com.ale.dao.ClientDAO;
import br.com.ale.dao.CredentialDAO;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.Credential;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateCredentialRequest;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class AuthService {

    private final ConnectionProvider connectionProvider;
    private final CredentialDAO credentialDAO = new CredentialDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final SimpleTokenGenerator simpleTokenGenerator = new SimpleTokenGenerator();
    private final PlainTextPasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    public AuthService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public long register(CreateCredentialRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            Client client = clientDAO.selectByDocument(conn, request.document())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found [document=" + request.document() + "]"
                            )
                    );

            String hashPassword = passwordEncoder.encode(request.password());

            long credentialId =
                    credentialDAO.insert(
                            conn,
                            client.getId(),
                            client.getDocument(),
                            hashPassword
                    );

            return credentialId;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while registering client " +
                            "[document=" + request.document() + "]",
                    e
            );
        }
    }

    public AuthToken authenticate(CreateAuthenticationRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            Credential credential = credentialDAO.selectByDocument(conn, request.document())
                    .orElseThrow(() -> new InvalidCredentialsException(
                            "Invalid credential [document=" + request.document() + "]"
                    ));

            if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
                throw new InvalidCredentialsException(
                        "Invalid credential [document=" + request.document() + "]"
                );
            }

            return simpleTokenGenerator.generate(credential.getClientId());

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while authentication client " +
                            "[document=" + request.document() + "]",
                    e
            );
        }
    }
}
