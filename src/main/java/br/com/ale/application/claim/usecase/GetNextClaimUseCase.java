package br.com.ale.application.claim.usecase;

import br.com.ale.application.claim.command.GetNextClaimCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

public class GetNextClaimUseCase {

    private final AccountService accountService;
    private final JwtService jwtService;

    public GetNextClaimUseCase(
            AccountService accountService,
            JwtService jwtService
    ) {
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public Instant execute(GetNextClaimCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());

        Account account = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedOperationException("Account not found"));

        return accountService.selectNextClaimById(account.getAccountNumber());
    }
}
