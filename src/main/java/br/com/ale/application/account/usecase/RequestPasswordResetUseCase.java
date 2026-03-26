package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.ChangePasswordSenderCommand;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.email.EmailVerificationSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RequestPasswordResetUseCase {

    private final ClientService clientService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationSender emailVerificationSender;

    public RequestPasswordResetUseCase(ClientService clientService,
                                       EmailVerificationService emailVerificationService,
                                       EmailVerificationSender emailVerificationSender) {
        this.clientService = clientService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationSender = emailVerificationSender;
    }


    public void execute(ChangePasswordSenderCommand command) {

        Client client = clientService.getClientByEmail(command.email());

        String token = UUID.randomUUID().toString();

        emailVerificationService.create(
                new CreateEmailVerificationRequest(
                        client.getId(),
                        token,
                        EmailVerificationType.PASSWORD_RESET,
                        Instant.now().plus(1, ChronoUnit.DAYS),
                        null
                )
        );

        emailVerificationSender.sendPasswordReset(client, token);
    }
}
