package br.com.ale.domain.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Account {

    private final long id;
    private final long clientId;
    private final String accountNumber;
    private final AccountType accountType;
    private final AccountStatus status;
    private BigDecimal balance = BigDecimal.ZERO;
    private String publicKey;
    private Instant nextFreeAssetAt;

    public Account(
            long id,
            long clientId,
            String accountNumber,
            AccountType accountType,
            BigDecimal balance,
            AccountStatus status,
            String publicKey,
            Instant nextFreeAssetAt
    ) {
        this.id = id;
        this.clientId = validateClientId(clientId);
        this.accountNumber = validateAccountNumber(accountNumber);
        this.accountType = Objects.requireNonNull(accountType);
        this.balance = validateBalance(balance);
        this.status = Objects.requireNonNull(status);
        this.publicKey = publicKey;
        this.nextFreeAssetAt = nextFreeAssetAt;
    }

    public Account(
            long id,
            long clientId,
            String accountNumber,
            AccountType accountType,
            AccountStatus status
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
            throw new IllegalArgumentException(
                    "Client id must be positive " + "[clientId=" + clientId + "]"
            );
        }
        return clientId;
    }

    private String validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number cannot be blank" + "[accountNumber=" + accountNumber + "]"
            );
        }
        return accountNumber;
    }

    private BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Account balance cannot be negative" + "[balance=" + balance + "]"
            );
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

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
