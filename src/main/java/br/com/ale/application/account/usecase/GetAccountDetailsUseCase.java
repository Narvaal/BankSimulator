package br.com.ale.application.account.usecase;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.service.AccountService;
import br.com.ale.service.auth.AuthService;

public class GetAccountDetailsUseCase {

    private final AccountService accountService;
    private final AuthService authService;

    public GetAccountDetailsUseCase(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    public AccountDetailsResponse execute(long accountId, String token) {
        TokenClaims claims = authService.validateToken(token);
        Account account = accountService.getAccountById(accountId);

        if (claims.clientId() != account.getClientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this account"
            );
        }

        return accountService.getAccountDetailsById(accountId);
    }
}
