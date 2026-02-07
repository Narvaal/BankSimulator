package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.AuthService;
import java.math.BigDecimal;

public class DepositAccountUseCase {

    private final AccountService accountService;
    private final AuthService authService;

    public DepositAccountUseCase(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    public void execute(DepositAccountCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        TokenClaims claims = authService.validateToken(command.token());
        Account account = accountService.getAccountById(command.accountId());

        if (claims.clientId() != account.getClientId()) {
            throw new UnauthorizedOperationException("Authenticated client does not own this account");
        }

        accountService.credit(account.getAccountNumber(), command.amount());
    }
}
