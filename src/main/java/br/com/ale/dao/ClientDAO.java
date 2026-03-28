package br.com.ale.dao;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateClientRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    public long insert(Connection conn, CreateClientRequest request) {

        String sql = """
                INSERT INTO client (
                    name,
                    email,
                    password,
                    provider,
                    provider_id,
                    email_verified,
                    picture
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, request.name());
            stmt.setString(2, request.email());
            stmt.setString(3, request.password());
            stmt.setString(4, String.valueOf(request.provider()));
            stmt.setString(5, request.providerId());
            stmt.setBoolean(6, request.emailVerified());
            stmt.setString(7, request.picture());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {

                throw new RuntimeException(
                        "Failed to insert client [name=" + request.name() +
                                ", email=" + request.email() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }

                throw new RuntimeException(
                        "Failed to retrieve client id [name=" + request.name() +
                                ", email=" + request.email() + "]"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting client " +
                            "[name=" + request.name() +
                            ", email=" + request.email() + "]",
                    e
            );
        }
    }

    public int update(Connection conn, UpdateClientRequest request) {

        String sql = """
                UPDATE client
                   SET password = ?
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, request.password());
            stmt.setLong(2, request.id());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating client " +
                            "[clientId=" + request.id() +
                            ", password=" + request.password() + "]",
                    e
            );
        }
    }

    public int activate(Connection conn, Long clientId) {

        String sql = """
                UPDATE client
                   SET email_verified = TRUE
                 WHERE id = ?
                   AND email_verified = FALSE
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while activating client " +
                            "[clientId=" + clientId + "]",
                    e
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
                    "Database error while deleting client " +
                            "[clientId=" + id + "]",
                    e
            );
        }
    }

    public Optional<Client> selectById(Connection conn, long id) {

        String sql = """
                SELECT  id,
                        name,
                        email,
                        password,
                        provider,
                        provider_id,
                        email_verified,
                        picture
                FROM client WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("email"),
                                    rs.getString("password"),
                                    Provider.valueOf(rs.getString("provider")),
                                    rs.getString("provider_id"),
                                    rs.getBoolean("email_verified"),
                                    rs.getString("picture")
                            )
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting client " +
                            "[clientId=" + id + "]",
                    e
            );
        }
    }

    public Optional<Client> selectByEmail(Connection conn, String email) {

        String sql = """
                SELECT  id,
                        name,
                        email,
                        password,
                        provider,
                        provider_id,
                        email_verified,
                        picture
                FROM client WHERE email = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("email"),
                                    rs.getString("password"),
                                    Provider.valueOf(rs.getString("provider")),
                                    rs.getString("provider_id"),
                                    rs.getBoolean("email_verified"),
                                    rs.getString("picture")
                            )
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting client " +
                            "[clientEmail=" + email + "]",
                    e
            );
        }
    }

    public Optional<Client> selectByProviderAndId(Connection conn, Provider provider, String providerId) {

        String sql = """
                SELECT  id,
                        name,
                        email,
                        password,
                        provider,
                        provider_id,
                        email_verified,
                        picture
                FROM client WHERE provider = ? AND provider_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(provider));
            stmt.setString(2, providerId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("email"),
                                    rs.getString("password"),
                                    Provider.valueOf(rs.getString("provider")),
                                    rs.getString("provider_id"),
                                    rs.getBoolean("email_verified"),
                                    rs.getString("picture")
                            )
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting client " +
                            "[provider=" + String.valueOf(provider) + ", "
                            + "providerId=" + providerId + "]",
                    e
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
                       public_key,
                        next_free_asset_at
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
                                    AccountType.valueOf(rs.getString("account_type")),
                                    rs.getBigDecimal("balance"),
                                    AccountStatus.valueOf(rs.getString("status")),
                                    rs.getString("public_key"),
                                    rs.getTimestamp("next_free_asset_at").toInstant()
                            )
                    );
                }

                return accounts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting account " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }


}
