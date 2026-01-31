package br.com.ale.application.account.command;

import java.math.BigDecimal;

public record DepositAccountCommand(long accountId, BigDecimal amount, String token) {
}
