package br.com.ale.dto;

import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionRequest(Long fromAccountId, String fromAccountNumber, Long toAccountId,
                                       String toAccountNumber, BigDecimal amount, TransactionType type,
                                       TransactionStatus status, String signature) {
}
