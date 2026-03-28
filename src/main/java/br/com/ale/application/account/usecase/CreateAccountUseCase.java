package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.auth.PasswordValidator;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.account.AccountNumberGenerator;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.email.EmailVerificationSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CreateAccountUseCase {

    private final AccountService accountService;
    private final ClientService clientService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationSender emailVerificationSender;

    public CreateAccountUseCase(
            AccountService accountService,
            ClientService clientService,
            AccountNumberGenerator accountNumberGenerator,
            EmailVerificationService emailVerificationService,
            EmailVerificationSender emailVerificationSender
    ) {
        this.accountService = accountService;
        this.accountNumberGenerator = accountNumberGenerator;
        this.clientService = clientService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationSender = emailVerificationSender;
    }

    public void execute(CreateAccountCommand command) {

        PasswordValidator.validate(command.password());
        
        Client client = clientService.createClient(
                new CreateClientRequest(
                        command.name(),
                        command.email(),
                        PasswordHasher.hash(command.password()),
                        Provider.LOCAL,
                        null,
                        false,
                        null
                )
        );

        String token = UUID.randomUUID().toString();

        try {
            emailVerificationSender.sendVerificationEmail(client, token);

            emailVerificationService.create(
                    new CreateEmailVerificationRequest(
                            client.getId(),
                            token,
                            EmailVerificationType.EMAIL_VERIFICATION,
                            Instant.now().plus(1, ChronoUnit.DAYS),
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

        } catch (Exception e) {
            clientService.deleteClient(client.getId());
            throw new RuntimeException("Failed to send verification email, account not created.", e);
        }
    }
}