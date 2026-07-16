package br.com.ale.domain.exception;

public class AccountNotFoundException extends BusinessRuleException {
  public AccountNotFoundException(long accountId) {
    super("Account not found [accountId=" + accountId + "]");
  }
}
