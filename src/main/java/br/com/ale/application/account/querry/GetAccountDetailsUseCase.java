package br.com.ale.application.account.querry;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.JwtService;

public class GetAccountDetailsUseCase {

    private final AccountService accountService;
    private final JwtService jwtService;

    public GetAccountDetailsUseCase(AccountService accountService, JwtService jwtService) {
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public AccountDetailsResponse execute(String token) {

        long clientId = jwtService.extractClientId(token);

        Account account = accountService.getAccountByClientId(clientId).orElseThrow(
                () -> new UnauthorizedOperationException("Account not found"));

        return accountService.getAccountDetailsById(account.getId());
    }
}
