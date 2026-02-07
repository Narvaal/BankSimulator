package br.com.ale.service.account;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.TransactionDAO;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.SignatureService;
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
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ConnectionProvider connectionProvider;
    private final KeyPairService keyPairService = new KeyPairService();
    private final PrivateKeyStorage privateKeyStorage;

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

            try {
                KeyPair keyPair = keyPairService.generate();
                String publicKey = keyPairService.encodePublicKey(keyPair);

                long accountId = accountDAO.insert(conn, request, publicKey);

                privateKeyStorage.save(accountId, keyPair.getPrivate().getEncoded());

                conn.commit();

                return new Account(
                        accountId,
                        request.clientId(),
                        request.accountNumber(),
                        request.accountType(),
                        java.math.BigDecimal.ZERO,
                        request.status(),
                        publicKey
                );

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating account " +
                            "[accountNumber=" + request.accountNumber() +
                            ", clientId=" + request.clientId() + "]",
                    e
            );
        }
    }

    public Account getAccountByNumber(String accountNumber) {
        try (Connection conn = connectionProvider.getConnection()) {
            return accountDAO.selectByNumber(conn, accountNumber)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Account not found [accountNumber=" + accountNumber + "]"
                            )
                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting account " +
                            "[accountNumber=" + accountNumber + "]",
                    e
            );
        }
    }

    public Account getAccountById(long accountId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return accountDAO.selectById(conn, accountId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Account not found [accountId=" + accountId + "]"
                            )
                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting account " +
                            "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    public Optional<Account> getAccountByClientId(long clientId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return accountDAO.selectById(conn, clientId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting account " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }

    public AccountDetailsResponse getAccountDetailsById(long accountId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return accountDAO.selectDetailsById(conn, accountId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Account not found [accountId=" + accountId + "]"
                            )
                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting account details " +
                            "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    public void updateAccount(UpdateAccountRequest request) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int rows = accountDAO.update(conn, request);
                if (rows == 0) {
                    throw new RuntimeException(
                            "Account not found [accountId=" + request.id() + "]"
                    );
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while updating account " +
                            "[accountId=" + request.id() + "]",
                    e
            );
        }
    }

    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {

        if (fromAccountId == toAccountId) {
            throw new RuntimeException(
                    "Not allowed transfer to the same account " +
                            "[fromAccountId=" + fromAccountId +
                            ", toAccountId=" + toAccountId + "]"
            );
        }

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account from = accountDAO.selectById(conn, fromAccountId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Source account not found " +
                                                "[fromAccountId=" + fromAccountId + "]"
                                )
                        );

                Account to = accountDAO.selectById(conn, toAccountId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Destination account not found " +
                                                "[toAccountId=" + toAccountId + "]"
                                )
                        );

                Instant timestamp = Instant.now();

                String message = TransactionMessageBuilder.build(
                        fromAccountId,
                        toAccountId,
                        amount,
                        timestamp
                );

                PrivateKey privateKey = privateKeyStorage.get(fromAccountId);
                if (privateKey == null) {
                    throw new RuntimeException(
                            "Private key not found [fromAccountId=" + fromAccountId + "]"
                    );
                }

                String signature = SignatureService.sign(message, privateKey);

                long transactionId = transactionDAO.insert(
                        conn,
                        new CreateTransactionRequest(
                                fromAccountId,
                                from.getAccountNumber(),
                                toAccountId,
                                to.getAccountNumber(),
                                amount,
                                TransactionType.TRANSFERENCE,
                                TransactionStatus.PENDING,
                                signature
                        )
                );

                int debited = accountDAO.debit(
                        conn,
                        new CreateBalanceOperationRequest(from.getAccountNumber(), amount)
                );

                if (debited == 0) {
                    transactionDAO.update(
                            conn,
                            new UpdateTransactionRequest(transactionId, TransactionStatus.FAILED)
                    );
                    throw new RuntimeException(
                            "Insufficient balance [fromAccountId=" + fromAccountId + "]"
                    );
                }

                int credited = accountDAO.credit(
                        conn,
                        new CreateBalanceOperationRequest(to.getAccountNumber(), amount)
                );

                if (credited == 0) {
                    transactionDAO.update(
                            conn,
                            new UpdateTransactionRequest(transactionId, TransactionStatus.FAILED)
                    );
                    throw new RuntimeException(
                            "Failed to credit destination [toAccountId=" + toAccountId + "]"
                    );
                }

                transactionDAO.update(
                        conn,
                        new UpdateTransactionRequest(transactionId, TransactionStatus.COMPLETE)
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while transferring " +
                            "[fromAccountId=" + fromAccountId +
                            ", toAccountId=" + toAccountId +
                            ", amount=" + amount + "]",
                    e
            );
        }
    }

    public void credit(String accountNumber, BigDecimal amount) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = accountDAO.selectByNumber(conn, accountNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found [accountNumber=" + accountNumber + "]"
                                )
                        );

                Instant timestamp = Instant.now();

                String message = TransactionMessageBuilder.build(
                        null,
                        account.getId(),
                        amount,
                        timestamp
                );

                PrivateKey privateKey = privateKeyStorage.get(account.getId());
                if (privateKey == null) {
                    throw new RuntimeException(
                            "Private key not found [accountNumber=" + accountNumber + "]"
                    );
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
                        new CreateBalanceOperationRequest(accountNumber, amount)
                );

                if (credited == 0) {
                    transactionDAO.update(
                            conn,
                            new UpdateTransactionRequest(transactionId, TransactionStatus.FAILED)
                    );
                    throw new RuntimeException(
                            "Failed to credit account [accountNumber=" + accountNumber +
                                    ", amount=" + amount + "]"
                    );
                }

                transactionDAO.update(
                        conn,
                        new UpdateTransactionRequest(transactionId, TransactionStatus.COMPLETE)
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while crediting " +
                            "[accountNumber=" + accountNumber +
                            ", amount=" + amount + "]",
                    e
            );
        }
    }

    public void debit(String accountNumber, BigDecimal amount) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = accountDAO.selectByNumber(conn, accountNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found [accountNumber=" + accountNumber + "]"
                                )
                        );

                Instant timestamp = Instant.now();

                String message = TransactionMessageBuilder.build(
                        account.getId(),
                        null,
                        amount,
                        timestamp
                );

                PrivateKey privateKey = privateKeyStorage.get(account.getId());
                if (privateKey == null) {
                    throw new RuntimeException(
                            "Private key not found [accountNumber=" + accountNumber + "]"
                    );
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
                        new CreateBalanceOperationRequest(accountNumber, amount)
                );

                if (debited == 0) {
                    transactionDAO.update(
                            conn,
                            new UpdateTransactionRequest(transactionId, TransactionStatus.FAILED)
                    );
                    throw new RuntimeException(
                            "Insufficient balance [accountNumber=" + accountNumber +
                                    ", amount=" + amount + "]"
                    );
                }

                transactionDAO.update(
                        conn,
                        new UpdateTransactionRequest(transactionId, TransactionStatus.COMPLETE)
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while debiting " +
                            "[accountNumber=" + accountNumber +
                            ", amount=" + amount + "]",
                    e
            );
        }
    }
}
