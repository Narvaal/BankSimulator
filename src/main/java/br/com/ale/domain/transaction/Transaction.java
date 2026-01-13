package br.com.ale.domain.transaction;

import java.math.BigDecimal;

public class Transaction {
    private final long id;
    private final Long fromAccountId;
    private final String fromAccountNumber;
    private final Long toAccountId;
    private final String toAccountNumber;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String signature;

    public Transaction(long id, Long fromAccountId, String fromAccountNumber, Long toAccountId, String toAccountNumber,
                       BigDecimal amount, TransactionType type, TransactionStatus status, String signature) {
        this.id = id;
        this.fromAccountId = setFromAccountId(fromAccountId);
        this.fromAccountNumber = setFromAccountNumber(fromAccountNumber);
        this.toAccountId = setToAccountId(toAccountId);
        this.toAccountNumber = setToAccountNumber(toAccountNumber);
        this.amount = setAmount(amount);
        this.type = setType(type);
        this.status = setStatus(status);
        this.signature = setSignature(signature);
    }

    public Long setFromAccountId(Long fromAccountId) {
        if (fromAccountId == null || fromAccountId.compareTo(Long.MIN_VALUE) < 0) {
            throw new IllegalArgumentException(
                    "Transaction fromAccountId must be positive " + "[fromAccountId=" + fromAccountId + "]"
            );
        }
        return fromAccountId;
    }

    public String setFromAccountNumber(String fromAccountNumber) {
        if (fromAccountNumber == null || fromAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction fromAccountNumber must not be blank " + "[fromAccountNumber=" + fromAccountNumber + "]"
            );
        }
        return fromAccountNumber;
    }

    public Long setToAccountId(Long toAccountId) {
        if (toAccountId == null || toAccountId.compareTo(Long.MIN_VALUE) < 0) {
            throw new IllegalArgumentException(
                    "Transaction toAccountId must be positive " + "[toAccountId=" + toAccountId + "]"
            );
        }
        return toAccountId;
    }

    public String setToAccountNumber(String toAccountNumber) {
        if (toAccountNumber == null || toAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction toAccountNumber must not be blank " + "[toAccountNumber=" + toAccountNumber + "]"
            );
        }
        return toAccountNumber;
    }

    public BigDecimal setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Transaction Amount must be positive " + "[amount=" + amount + "]"
            );
        }
        return amount;
    }

    public TransactionType setType(TransactionType type) {
        if (type == null || type.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction type must not be blank " + "[type=" + type + "]"
            );
        }
        return type;
    }

    public TransactionStatus setStatus(TransactionStatus status) {
        if (status == null || status.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction status must not be blank " + "[status=" + status + "]"
            );
        }
        return status;
    }

    public String setSignature(String signature) {
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction signature must not be blank " + "[signature=" + signature + "]"
            );
        }
        return signature;
    }

    public long getId() {
        return id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getSignature() {
        return signature;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }
}
