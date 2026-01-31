package br.com.ale.application.transaction.query;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.domain.transaction.Transaction;
import br.com.ale.service.AccountService;
import br.com.ale.service.TransactionService;
import br.com.ale.service.auth.AuthService;
import java.util.List;

public class ListTransfersByAccountUseCase {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthService authService;

    public ListTransfersByAccountUseCase(
            TransactionService transactionService,
            AccountService accountService,
            AuthService authService
    ) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.authService = authService;
    }

    public List<Transaction> execute(long accountId, String token) {
        TokenClaims claims = authService.validateToken(token);
        Account account = accountService.getAccountById(accountId);

        if (claims.clientId() != account.getClientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this account"
            );
        }

        return transactionService.listTransfersByAccount(accountId);
    }
}
