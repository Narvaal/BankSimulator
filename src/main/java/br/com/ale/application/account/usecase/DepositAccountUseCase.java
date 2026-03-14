package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;

import java.math.BigDecimal;

public class DepositAccountUseCase {

    private final AccountService accountService;
    private final ClientService clientService;

    public DepositAccountUseCase(AccountService accountService, ClientService clientService) {
        this.accountService = accountService;
        this.clientService = clientService;
    }

    public void execute(DepositAccountCommand command) {

        Client client = clientService.getClientByEmail(command.email());

        Account account = accountService.getAccountByClientId(client.getId()).orElseThrow(() ->
                new InvalidCredentialsException("Account not found")
        );

        accountService.credit(account.getAccountNumber(), new BigDecimal(command.amount()));
    }
}
