package br.com.ale.dto;

import br.com.ale.domain.transaction.TransactionStatus;

public record UpdateTransactionRequest(long id, TransactionStatus status) {
}
