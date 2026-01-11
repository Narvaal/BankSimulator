package br.com.ale.service;

import br.com.ale.dao.AccountDAO;
import br.com.ale.domain.Account;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.UpdateAccountRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.crypto.PrivateKeyStorage;

import java.security.KeyPair;
import java.sql.Connection;

public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();
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
}
