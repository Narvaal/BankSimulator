package br.com.ale.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {

    private long id;
    private long clientId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance = BigDecimal.ZERO;
    private String status;
    private String publicKey;

    public Account(
            long id,
            long clientId,
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String status,
            String publicKey
    ) {
        this.id = id;
        this.clientId = validateClientId(clientId);
        this.accountNumber = validateAccountNumber(accountNumber);
        this.accountType = Objects.requireNonNull(accountType);
        this.balance = validateBalance(balance);
        this.status = Objects.requireNonNull(status);
        this.publicKey = publicKey;
    }

    public Account(
            long id,
            long clientId,
            String accountNumber,
            String accountType,
            String status
    ) {
        this.id = id;
        this.clientId = validateClientId(clientId);
        this.accountNumber = validateAccountNumber(accountNumber);
        this.accountType = Objects.requireNonNull(accountType);
        this.balance = validateBalance(balance);
        this.status = Objects.requireNonNull(status);
    }

    private long validateClientId(long clientId) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client id must be positive");
        }
        return clientId;
    }

    private String validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be blank");
        }
        return accountNumber;
    }

    private BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        return balance;
    }

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public String getPublicKey() {
        return  publicKey;
    }
}
