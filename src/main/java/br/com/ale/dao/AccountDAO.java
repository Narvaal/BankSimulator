package br.com.ale.dao;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateBalanceOperationRequest;
import br.com.ale.dto.UpdateAccountRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

public class AccountDAO {

    private static Account mapRow(ResultSet rs) throws SQLException {

        return new Account(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("account_number"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getBigDecimal("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getString("public_key"),
                rs.getTimestamp("next_free_asset_at").toInstant()
        );
    }

    public long insert(Connection conn, CreateAccountRequest request, String publicKey) {

        String sql = """
                INSERT INTO account (client_id, account_number, account_type, status, public_key)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.clientId());
            stmt.setString(2, request.accountNumber());
            stmt.setString(3, request.accountType().name());
            stmt.setString(4, request.status().name());
            stmt.setString(5, publicKey);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert account [clientId=" + request.clientId() +
                                ", accountNumber=" + request.accountNumber() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException(
                        "Failed to retrieve account id [clientId=" + request.clientId() +
                                ", accountNumber=" + request.accountNumber() + "]"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting account " +
                            "[clientId=" + request.clientId() +
                            ", accountNumber=" + request.accountNumber() + "]",
                    e
            );
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
            stmt.setString(2, request.accountType().name());
            stmt.setString(3, request.status().name());
            stmt.setLong(4, request.id());

            return stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Database error while updating account " +
                            "[accountId=" + request.id() +
                            ", accountNumber=" + request.accountNumber() + "]",
                    e
            );
        }
    }

    public Optional<Account> selectById(Connection conn, long accountId) {

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
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            mapRow(rs)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting account " +
                            "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    public Optional<Account> selectByClientId(Connection conn, long clientId) {

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

                if (rs.next()) {
                    return Optional.of(
                            mapRow(rs)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting account " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }

    public Optional<AccountDetailsResponse> selectDetailsById(Connection conn, long accountId) {

        String sql = """
                    SELECT
                    a.id,
                    a.client_id,
                    a.account_number,
                    a.account_type,
                    a.balance,
                    a.status,
                    a.public_key,
                    a.created_at,
                    a.updated_at,
                    a.next_free_asset_at,
                
                    c.name,
                    c.picture,
                    c.email_verified,
                    c.provider
                FROM account a
                JOIN client c ON c.id = a.client_id
                WHERE a.id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            new AccountDetailsResponse(
                                    rs.getLong("id"),
                                    rs.getLong("client_id"),
                                    rs.getString("account_number"),
                                    AccountType.valueOf(rs.getString("account_type")),
                                    rs.getBigDecimal("balance"),
                                    AccountStatus.valueOf(rs.getString("status")),
                                    rs.getString("public_key"),
                                    rs.getTimestamp("created_at").toInstant(),
                                    rs.getTimestamp("updated_at").toInstant(),
                                    rs.getTimestamp("next_free_asset_at").toInstant(),

                                    rs.getString("name"),
                                    rs.getString("picture"),
                                    rs.getBoolean("email_verified"),
                                    Provider.valueOf(rs.getString("provider"))
                            )
                    );
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting account details " +
                            "[accountId=" + accountId + "]",
                    e
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
                       public_key,
                       next_free_asset_at
                  FROM account
                 WHERE account_number = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            mapRow(rs)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting account " +
                            "[accountNumber=" + accountNumber + "]",
                    e
            );
        }
    }

    public int debit(Connection conn, CreateBalanceOperationRequest request) {

        String sql = """
                UPDATE account
                SET balance = balance - ?,
                    updated_at = now()
                WHERE account_number = ?
                  AND balance >= ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, request.amount());
            stmt.setString(2, request.accountNumber());
            stmt.setBigDecimal(3, request.amount());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while debiting account " +
                            "[accountNumber=" + request.accountNumber() +
                            ", amount=" + request.amount() + "]",
                    e
            );
        }
    }

    public int credit(Connection conn, CreateBalanceOperationRequest request) {

        String sql = """
                UPDATE account
                SET balance = balance + ?,
                    updated_at = now()
                WHERE account_number = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, request.amount());
            stmt.setString(2, request.accountNumber());

            return stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Database error while crediting account " +
                            "[accountNumber=" + request.accountNumber() +
                            ", amount=" + request.amount() + "]",
                    e
            );
        }
    }

    public Optional<Instant> tryClaimAssetUnity(Connection conn, String accountNumber) {

        String sql = """
                UPDATE account
                SET next_free_asset_at = now() + interval '2 hours'
                WHERE account_number = ?
                AND next_free_asset_at <= now()
                RETURNING next_free_asset_at;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getTimestamp("next_free_asset_at").toInstant());
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while claiming free asset [accountId=" + accountNumber + "]",
                    e
            );
        }
    }

    public Instant selectNextClaimById(Connection conn, String accountNumber) {

        String sql = """
                SELECT next_free_asset_at
                  FROM account
                 WHERE account_number = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getDate("next_free_asset_at").toInstant();
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting next claim instant account " +
                            "[accountNumber=" + accountNumber + "]",
                    e
            );
        }
    }
}
