package br.com.ale.domain;

import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;

import java.math.BigDecimal;

public class Transaction {
    private long id;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String signature;

    public Transaction (long id, Long fromAccountId, String fromAccountNumber, Long toAccountId, String toAccountNumber,
                        BigDecimal amount, TransactionType type, TransactionStatus status, String signature)
    {
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
            throw new IllegalArgumentException("Transaction fromAccountId cannot be negative");
        }
        return fromAccountId;
    }
    public String setFromAccountNumber(String fromAccountNumber) {
        if (fromAccountNumber == null || fromAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Transaction fromAccountNumber cannot be blank");
        }
        return fromAccountNumber;
    }

    public Long setToAccountId(Long toAccountId) {
        if (toAccountId == null || toAccountId.compareTo(Long.MIN_VALUE) < 0) {
            throw new IllegalArgumentException("Transaction toAccountId cannot be negative");
        }
        return toAccountId;
    }

    public String setToAccountNumber(String toAccountNumber) {
        if (toAccountNumber == null || toAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Transaction toAccountNumber cannot be blank");
        }
        return toAccountNumber;
    }

    public BigDecimal setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return amount;
    }

    public TransactionType setType(TransactionType type) {
        if (type == null || type.name().isBlank()) {
            throw new IllegalArgumentException("Transaction type cannot be blank");
        }
        return type;
    }

    public TransactionStatus setStatus(TransactionStatus status) {
        if (status == null || status.name().isBlank()) {
            throw new IllegalArgumentException("Transaction status cannot be blank");
        }
        return status;
    }

    public String setSignature(String signature) {
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Transaction signature cannot be blank");
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
}
