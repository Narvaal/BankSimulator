package br.com.ale.service;

import br.com.ale.dao.ClientDAO;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.HashAccountNumberGenerator;

import java.sql.Connection;
import java.util.Optional;

public class ClientService {

    private final ClientDAO clientDAO = new ClientDAO();
    private final HashAccountNumberGenerator hashAccountNumberGenerator = new HashAccountNumberGenerator();
    private final ConnectionProvider connectionProvider;
    public ClientService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;

    }

    public Client createClient(CreateClientRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long id = clientDAO.insert(conn, request);

                conn.commit();

                return new Client(
                        id,
                        request.name(),
                        request.email(),
                        request.password(),
                        request.provider(),
                        request.providerId(),
                        request.emailVerified(),
                        request.picture()
                );

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating client " +
                            "[name=" + request.name() +
                            ", email=" + request.email() + "]",
                    e
            );
        }
    }

    public void updateClient(UpdateClientRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int rowsAffected = clientDAO.update(conn, request);

                if (rowsAffected == 0) {
                    throw new RuntimeException(
                            "Client not found [clientId=" + request.id() + "]"
                    );
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while updating client " +
                            "[id=" + request.id() +
                            ", name=" + request.name() + "]",
                    e
            );
        }
    }

    public Client getClientByEmail(String email) {

        try (Connection conn = connectionProvider.getConnection()) {

            return clientDAO.selectByEmail(conn, email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found [email=" + email + "]"
                            )
                    );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting client " +
                            "[email=" + email + "]",
                    e
            );
        }
    }

    public Optional<Client> getClientByProviderAndId(Provider provider, String providerId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return clientDAO.selectByProviderAndId(conn, provider,providerId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting client " +
                            "[providerId=" + providerId + "]",
                    e
            );
        }
    }

    public Client getClientById(long clientId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return clientDAO.selectById(conn, clientId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found [clientId=" + clientId + "]"
                            )
                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting client " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }

    public void deleteClient(long id) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int rowsAffected = clientDAO.deleteById(conn, id);

                if (rowsAffected == 0) {
                    throw new RuntimeException(
                            "Client not found [id=" + id + "]"
                    );
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while deleting client " +
                            "[id=" + id + "]",
                    e
            );
        }
    }
}
