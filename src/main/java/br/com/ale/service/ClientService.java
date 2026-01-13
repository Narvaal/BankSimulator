package br.com.ale.service;

import br.com.ale.dao.ClientDAO;
import br.com.ale.domain.client.Client;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class ClientService {

    private final ClientDAO clientDAO = new ClientDAO();
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
                        request.document()
                );

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating client " +
                            "[name=" + request.name() +
                            ", document=" + request.document() + "]",
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

    public Client getClientByDocument(String document) {

        try (Connection conn = connectionProvider.getConnection()) {

            return clientDAO.selectByDocument(conn, document)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found [document=" + document + "]"
                            )
                    );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting client " +
                            "[document=" + document + "]",
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
