package br.com.ale.dto;

import java.math.BigDecimal;

public record BalanceOperationRequest(String accountNumber, BigDecimal amount) {
}
