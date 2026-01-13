package br.com.ale.service;

import br.com.ale.dao.TransactionDAO;
import br.com.ale.domain.transaction.Transaction;
import br.com.ale.dto.CreateTransactionRequest;
import br.com.ale.dto.UpdateTransactionRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ConnectionProvider connectionProvider;

    public TransactionService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            long transactionId = transactionDAO.insert(conn, request);

            return new Transaction(
                    transactionId,
                    request.fromAccountId(),
                    request.fromAccountNumber(),
                    request.toAccountId(),
                    request.toAccountNumber(),
                    request.amount(),
                    request.type(),
                    request.status(),
                    request.signature()
            );

        } catch (Exception e) {


            throw new RuntimeException(
                    "Service error while creating transaction " +
                            "[fromAccountId=" + request.fromAccountId() + ", "
                            + "toAccountId=" + request.toAccountId() + "]",
                    e
            );
        }
    }


    public void updateStatus(UpdateTransactionRequest request) {
        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            long rowsAffected = transactionDAO.update(conn, request);

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Transaction not found " +
                                "[id=" + request.id() + "]"
                );
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while updating transaction " +
                            "[id=" + request.id() + ", "
                            + "status=" + request.status().name() + "]",
                    e
            );
        }
    }
}
