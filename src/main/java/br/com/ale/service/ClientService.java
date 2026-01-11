package br.com.ale.service;

import br.com.ale.dao.ClientDAO;
import br.com.ale.domain.Client;
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

            long id = clientDAO.insert(
                    conn,
                    request.name(),
                    request.document()
            );

            conn.commit();

            return new Client(
                    id,
                    request.name(),
                    request.document()
            );

        } catch (Exception e) {
            throw new RuntimeException("Error - Create client", e);
        }
    }

    public void updateClient(UpdateClientRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            int rowsAffected = clientDAO.update(conn, request);

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Client not found with id: " + request.id()
                );
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Update client", e);
        }
    }

    public Client getClientByDocument(String document) {

        try (Connection conn = connectionProvider.getConnection()) {

            return clientDAO.selectByDocument(conn, document)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Client not found with document: " + document
                            )
                    );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error - Select client by document", e
            );
        }
    }

    public void deleteClient(long id) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            int rowsAffected = clientDAO.deleteById(conn, id);

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Client not found with id: " + id
                );
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Delete client", e);
        }
    }
}
