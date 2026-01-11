package br.com.ale.dao;

import br.com.ale.domain.Account;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.UpdateAccountRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AccountDAO {

    public long insert(Connection conn, CreateAccountRequest request, String publicKey) {

        String sql = """
        INSERT INTO account (client_id, account_number, account_type, status, public_key)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.clientId());
            stmt.setString(2, request.accountNumber());
            stmt.setString(3, request.accountType());
            stmt.setString(4, request.status());
            stmt.setString(5,publicKey);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Error - Insert account failed");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException("Error - Failed to retrieve generated account id");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error - Insert account", e);
        }
    }

    public int update(Connection conn, UpdateAccountRequest request) {

        String sql = """
            UPDATE account
               SET account_number = ?,
                   account_type   = ?,
                   status         = ?
             WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, request.accountNumber());
            stmt.setString(2, request.accountType());
            stmt.setString(3, request.status());
            stmt.setLong(4, request.id());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Update account with id: " + request.id(), e
            );
        }
    }

    public Optional<Account> selectByNumber(Connection conn, String accountNumber) {

        String sql = """
            SELECT id,
                   client_id,
                   account_number,
                   account_type,
                   balance,
                   status,
                   public_key
              FROM account
             WHERE account_number = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
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

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Select account with number: " + accountNumber, e
            );
        }
    }
}
