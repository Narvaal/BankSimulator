package br.com.ale.service;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.TransactionDAO;
import br.com.ale.domain.Account;
import br.com.ale.domain.Transaction;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.crypto.PrivateKeyStorage;
import br.com.ale.service.crypto.TransactionMessageBuilder;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;

public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final ConnectionProvider connectionProvider;
    private final KeyPairService keyPairService = new KeyPairService();
    private final PrivateKeyStorage privateKeyStorage;
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public AccountService(
            ConnectionProvider connectionProvider,
            PrivateKeyStorage privateKeyStorage
    ) {
        this.connectionProvider = connectionProvider;
        this.privateKeyStorage = privateKeyStorage;
    }

    public Account createAccount(CreateAccountRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            KeyPair keyPair = keyPairService.generate();

            String publicKey = keyPairService.encodePublicKey(keyPair);

            long accountId = accountDAO.insert(conn, request, publicKey);

            privateKeyStorage.save(
                    accountId,
                    keyPair.getPrivate().getEncoded()
            );

            conn.commit();

            return new Account(
                    accountId,
                    request.clientId(),
                    request.accountNumber(),
                    request.accountType(),
                    request.status()
            );

        } catch (Exception e) {
            throw new RuntimeException("Error - Create account", e);
        }
    }

    public Account getAccountByNumber(String accountNumber) {

        try (Connection conn = connectionProvider.getConnection()) {

            return accountDAO.selectByNumber(conn, accountNumber)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Account not found with number: " + accountNumber
                            )
                    );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error - Select account by number", e
            );
        }
    }

    public void updateAccount(UpdateAccountRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            int rowsAffected = accountDAO.update(conn, request);

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Account not found with id: " + request.id()
                );
            }

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Update account", e);
        }
    }

    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {

        if (fromAccountId == toAccountId) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        try (Connection conn = connectionProvider.getConnection()){

            conn.setAutoCommit(false);

            Account fromAccount = accountDAO
                    .selectById(conn, fromAccountId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Source account not found: " + fromAccountId
                            )
                    );

            Account toAccount = accountDAO
                    .selectById(conn, toAccountId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Destination account not found: " + toAccountId
                            )
                    );

            TransactionStatus transactionStatus = TransactionStatus.PENDING;
            TransactionType transactionType = TransactionType.TRANSFERENCE;

            Instant timestamp = Instant.now();

            String message = TransactionMessageBuilder.build(
                    fromAccountId,
                    toAccountId,
                    amount,
                    timestamp
            );

            PrivateKey privateKey = privateKeyStorage.get(fromAccountId);

            if (privateKey == null) {
                throw new RuntimeException("Private key not found for account: " + fromAccountId);
            }

            String signature = SignatureService.sign(message, privateKey);

            long transactionId = transactionDAO.insert(
                    conn,
                    new CreateTransactionRequest(
                            fromAccountId,
                            fromAccount.getAccountNumber(),
                            toAccountId,
                            toAccount.getAccountNumber(),
                            amount,
                            transactionType,
                            transactionStatus,
                            signature
                    )
            );

            int debited = accountDAO.debit(
                    conn,
                    new BalanceOperationRequest(
                            fromAccount.getAccountNumber(),
                            amount
                    )
            );

            if (debited == 0) {
                throw new RuntimeException("Insufficient balance for account: "
                        + fromAccount.getAccountNumber());
            }

            int credited = accountDAO.credit(
                    conn,
                    new BalanceOperationRequest(
                            toAccount.getAccountNumber(),
                            amount
                    )
            );

            if (credited == 0) {
                throw new RuntimeException("Failed to credit destination account: "
                        + toAccount.getAccountNumber());
            }

            transactionDAO.update(
                    conn,
                    new UpdateTransactionRequest(
                            transactionId,
                            TransactionStatus.COMPLETE
                    )
            );

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Update account", e);
        }
    }

    public void credit(String accountNumber, BigDecimal amount) {
        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            Account account = accountDAO
                    .selectByNumber(conn, accountNumber)
                    .orElseThrow(() ->
                            new RuntimeException("Account not found: " + accountNumber));

            Instant timestamp = Instant.now();

            String message = TransactionMessageBuilder.build(
                    null,
                    account.getId(),
                    amount,
                    timestamp
            );

            PrivateKey privateKey = privateKeyStorage.get(account.getId());

            if (privateKey == null) {
                throw new RuntimeException("Private key not found for account: " + account.getId());
            }

            String signature = SignatureService.sign(message, privateKey);

            long transactionId = transactionDAO.insert(
                    conn,
                    new CreateTransactionRequest(
                            null,
                            null,
                            account.getId(),
                            account.getAccountNumber(),
                            amount,
                            TransactionType.DEPOSIT,
                            TransactionStatus.PENDING,
                            signature
                    )
            );

            int credited = accountDAO.credit(
                    conn,
                    new BalanceOperationRequest(accountNumber, amount)
            );

            if (credited == 0) {
                throw new RuntimeException("Failed to credit account");
            }

            transactionDAO.update(
                    conn,
                    new UpdateTransactionRequest(transactionId, TransactionStatus.COMPLETE)
            );

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Credit account", e);
        }
    }

    public void debit(String accountNumber, BigDecimal amount) {
        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            Account account = accountDAO
                    .selectByNumber(conn, accountNumber)
                    .orElseThrow(() ->
                            new RuntimeException("Account not found: " + accountNumber));

            Instant timestamp = Instant.now();

            String message = TransactionMessageBuilder.build(
                    account.getId(), // from
                    null,            // to
                    amount,
                    timestamp
            );

            PrivateKey privateKey = privateKeyStorage.get(account.getId());
            if (privateKey == null) {
                throw new RuntimeException("Private key not found for account: " + account.getId());
            }

            String signature = SignatureService.sign(message, privateKey);

            long transactionId = transactionDAO.insert(
                    conn,
                    new CreateTransactionRequest(
                            account.getId(),
                            account.getAccountNumber(),
                            null,
                            null,
                            amount,
                            TransactionType.WITHDRAW,
                            TransactionStatus.PENDING,
                            signature
                    )
            );

            int debited = accountDAO.debit(
                    conn,
                    new BalanceOperationRequest(accountNumber, amount)
            );

            if (debited == 0) {
                throw new RuntimeException("Insufficient balance for account: " + accountNumber);
            }

            transactionDAO.update(
                    conn,
                    new UpdateTransactionRequest(transactionId, TransactionStatus.COMPLETE)
            );

            conn.commit();

        } catch (Exception e) {
            throw new RuntimeException("Error - Withdraw from account", e);
        }
    }
}
