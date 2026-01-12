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

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

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
                throw new RuntimeException("Error - Insert transaction failed");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException("Error - Failed to retrieve generated transaction id");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error - Insert transaction", e);
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
                    "Error - Update transfer with id: " + request.id(), e
            );
        }
    }

    public List<Transaction> select(Connection conn, Long accountId) {
        String sql = """
        SELECT id,
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

            stmt.setLong(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {

                List<Transaction> transactions = new ArrayList<>();

                while (rs.next()) {

                    TransactionType type =
                            TransactionType.valueOf(rs.getString("type"));

                    TransactionStatus status =
                            TransactionStatus.valueOf(rs.getString("status"));

                    transactions.add(
                        new Transaction(
                                rs.getLong("id"),
                                accountId,
                                rs.getString("from_account_number"),
                                rs.getLong("to_account_id"),
                                rs.getString("to_account_number"),
                                rs.getBigDecimal("amount"),
                                type,
                                status,
                                rs.getString("signature")
                        )
                    );
                }
                return transactions;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error - Select transactions from account number: " + accountId, e
            );
        }
    }
}
