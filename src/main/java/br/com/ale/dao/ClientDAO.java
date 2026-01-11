package br.com.ale.dao;

import br.com.ale.domain.Account;
import br.com.ale.domain.Client;
import br.com.ale.dto.UpdateClientRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    public long insert(Connection conn, String name, String document) {

        String sql = """
        INSERT INTO client (name, document)
        VALUES (?, ?)
        """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, name);
            stmt.setString(2, document);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Error - Insert client failed");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException("Error - Failed to retrieve generated client id");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error - Insert client", e);
        }
    }

    public int update(Connection conn, UpdateClientRequest request) {

        String sql = """
            UPDATE client
               SET name = ?
             WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, request.name());
            stmt.setLong(2, request.id());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Update client with id: " + request.id(), e
            );
        }
    }

    public int deleteById(Connection conn, long id) {

        String sql = """
            DELETE FROM client
             WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Delete client with id: " + id, e
            );
        }
    }

    public Optional<Client> selectById(Connection conn, long id) {

        String sql = """
            SELECT id, name, document
              FROM client
             WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("document")
                            )
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Select client with id: " + id, e
            );
        }
    }

    public Optional<Client> selectByDocument(Connection conn, String document) {

        String sql = """
            SELECT id, name, document
              FROM client
             WHERE document = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, document);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("document")
                            )
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Select client with document: " + document, e
            );
        }
    }

    public List<Account> selectAccountsByClientId(Connection conn, long clientId) {

        String sql = """
            SELECT id,
                   client_id,
                   account_number,
                   account_type,
                   balance,
                   status,
                   public_key
              FROM account
             WHERE client_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {

                List<Account> accounts = new ArrayList<>();

                while (rs.next()) {
                    accounts.add(
                            new Account(
                                    rs.getLong("id"),
                                    rs.getLong("client_id"),
                                    rs.getString("account_number"),
                                    rs.getString("account_type"),
                                    rs.getBigDecimal("balance"),
                                    rs.getString("status"),
                                    rs.getString("public_key")
                            )
                    );
                }

                return accounts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Select accounts of client id: " + clientId, e
            );
        }
    }
}
