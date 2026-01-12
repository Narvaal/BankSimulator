package br.com.ale.dao;

import br.com.ale.domain.Transaction;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.CreateTransactionRequest;
import br.com.ale.dto.UpdateTransactionRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public Long insert(Connection conn, CreateTransactionRequest request) {

        String sql = """
                INSERT INTO transactions (
                    from_account_id,
                    from_account_number,
                    to_account_id,
                    to_account_number,
                    amount,
                    type,
                    status,
                    signature
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setObject(1, request.fromAccountId(), Types.BIGINT);
            stmt.setString(2, request.fromAccountNumber());
            stmt.setObject(3, request.toAccountId(), Types.BIGINT);
            stmt.setString(4, request.toAccountNumber());
            stmt.setBigDecimal(5, request.amount());
            stmt.setString(6, request.type().name());
            stmt.setString(7, request.status().name());
            stmt.setString(8, request.signature());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert transaction " +
                                "[fromAccountId=" + request.fromAccountId() +
                                ", toAccountId=" + request.toAccountId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }

            throw new RuntimeException(
                    "Failed to retrieve transaction id " +
                            "[fromAccountId=" + request.fromAccountId() +
                            ", toAccountId=" + request.toAccountId() + "]"
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting transaction " +
                            "[fromAccountId=" + request.fromAccountId() +
                            ", toAccountId=" + request.toAccountId() + "]",
                    e
            );
        }
    }

    public int update(Connection conn, UpdateTransactionRequest request) {

        String sql = """
                UPDATE transactions
                   SET status = ?
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, request.status().name());
            stmt.setLong(2, request.id());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating transaction " +
                            "[transactionId=" + request.id() +
                            ", status=" + request.status().name() + "]",
                    e
            );
        }
    }

    public List<Transaction> selectFromAccountId(Connection conn, Long fromAccountId) {

        String sql = """
                SELECT id,
                       from_account_id,
                       from_account_number,
                       to_account_id,
                       to_account_number,
                       amount,
                       type,
                       status,
                       signature
                  FROM transactions
                 WHERE from_account_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, fromAccountId, Types.BIGINT);

            try (ResultSet rs = stmt.executeQuery()) {

                List<Transaction> transactions = new ArrayList<>();

                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }

                return transactions;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting transactions " +
                            "[fromAccountId=" + fromAccountId + "]",
                    e
            );
        }
    }

    public List<Transaction> selectToAccountId(Connection conn, Long toAccountId) {

        String sql = """
                SELECT id,
                       from_account_id,
                       from_account_number,
                       to_account_id,
                       to_account_number,
                       amount,
                       type,
                       status,
                       signature
                  FROM transactions
                 WHERE to_account_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, toAccountId, Types.BIGINT);

            try (ResultSet rs = stmt.executeQuery()) {

                List<Transaction> transactions = new ArrayList<>();

                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }

                return transactions;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting transactions " +
                            "[toAccountId=" + toAccountId + "]",
                    e
            );
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {

        return new Transaction(
                rs.getLong("id"),
                rs.getObject("from_account_id", Long.class),
                rs.getString("from_account_number"),
                rs.getObject("to_account_id", Long.class),
                rs.getString("to_account_number"),
                rs.getBigDecimal("amount"),
                TransactionType.valueOf(rs.getString("type")),
                TransactionStatus.valueOf(rs.getString("status")),
                rs.getString("signature")
        );
    }
}
