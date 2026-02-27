package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.auth.JwtService;

import java.math.BigDecimal;

public class DepositAccountUseCase {

    private final AccountService accountService;
    private final JwtService jwtService;

    public DepositAccountUseCase(AccountService accountService, JwtService jwtService) {
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public void execute(DepositAccountCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());
        Account account = accountService.getAccountByClientId(clientId).orElseThrow(() ->
                new InvalidCredentialsException("Account not found")
        );

        accountService.credit(account.getAccountNumber(), command.amount());
    }
}
