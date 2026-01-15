package br.com.ale.dto;

import java.math.BigDecimal;

public record CreateBalanceOperationRequest(String accountNumber, BigDecimal amount) {
}
