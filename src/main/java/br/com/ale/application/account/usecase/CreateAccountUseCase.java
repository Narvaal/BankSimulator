package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountNumberGenerator;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

public class CreateAccountUseCase {

    private final AccountService accountService;
    private final ClientService clientService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final JwtService jwtService;

    public CreateAccountUseCase(
            AccountService accountService,
            ClientService clientService,
            AccountNumberGenerator accountNumberGenerator,
            JwtService jwtService

    ) {
        this.accountService = accountService;
        this.clientService = clientService;
        this.accountNumberGenerator = accountNumberGenerator;
        this.jwtService = jwtService;
    }

    public AuthToken execute(CreateAccountCommand command) {

        String hashedPassword = PasswordHasher.hash(command.password());

        Client client = clientService.createClient(
                new CreateClientRequest(
                        command.name(),
                        command.email(),
                        hashedPassword,
                        Provider.LOCAL,
                        null,
                        false,
                        null
                )
        );

        String accountNumber = accountNumberGenerator.generate(client);

        accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        accountNumber,
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );

        String jwt = jwtService.generateToken(client.getId());

        return new AuthToken(
                client.getId(),
                jwt,
                Instant.now()
        );
    }
}
