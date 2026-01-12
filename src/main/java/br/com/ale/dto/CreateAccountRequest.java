package br.com.ale.dto;

import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;

public record CreateAccountRequest(long clientId, String accountNumber, AccountType accountType, AccountStatus status) {
}
