package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.ResendVerificationCommand;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.email.EmailVerificationSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public class ResendVerificationUseCase {

    private final ClientService clientService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationSender emailVerificationSender;

    public ResendVerificationUseCase(ClientService clientService,
                                     EmailVerificationService emailVerificationService,
                                     EmailVerificationSender emailVerificationSender
    ) {
        this.clientService = clientService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationSender = emailVerificationSender;
    }

    public void execute(ResendVerificationCommand command) {

        try {
            Client client = clientService.getClientByEmail(command.email());

            if (client.isEmailVerified()) {
                return;
            }

            Optional<EmailVerification> existing =
                    emailVerificationService.findActiveByClientId(
                            client.getId(),
                            EmailVerificationType.EMAIL_VERIFICATION
                    );

            if (existing.isPresent()) {
                emailVerificationSender.sendVerificationEmail(
                        client,
                        existing.get().getToken()
                );
                return;
            }

            String token = UUID.randomUUID().toString();

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

        } catch (Exception ignored) {

        }
    }
}
