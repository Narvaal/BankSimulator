package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Client;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.CreateCredentialRequest;
import br.com.ale.service.AccountService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountNumberGenerator;

public class CreateAccountUseCase {

    private final AccountService accountService;
    private final ClientService clientService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AuthService authService;

    public CreateAccountUseCase(
            AccountService accountService,
            ClientService clientService,
            AccountNumberGenerator accountNumberGenerator,
            AuthService authService
    ) {
        this.accountService = accountService;
        this.clientService = clientService;
        this.accountNumberGenerator = accountNumberGenerator;
        this.authService = authService;
    }

    public Account execute(CreateAccountCommand command) {

        Client client = clientService.createClient(
                new CreateClientRequest(
                        command.name(),
                        command.email()
                )
        );

        authService.register(
                new CreateCredentialRequest(
                        command.email(),
                        command.password()
                )
        );

        String accountNumber = accountNumberGenerator.generate(client);

        return accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        accountNumber,
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );
    }
}
