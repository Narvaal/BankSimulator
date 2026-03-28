package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.VerifyAccountCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

public class VerifyAccountUseCase {

    private final EmailVerificationService emailVerificationService;
    private final ClientService clientService;
    private final JwtService jwtService;

    public VerifyAccountUseCase(
            EmailVerificationService emailVerificationService,
            ClientService clientService,
            JwtService jwtService
    ) {
        this.emailVerificationService = emailVerificationService;
        this.clientService = clientService;
        this.jwtService = jwtService;
    }

    public AuthToken execute(VerifyAccountCommand command) {

        EmailVerification verification =
                emailVerificationService.confirmToken(
                        command.token(),
                        EmailVerificationType.EMAIL_VERIFICATION
                );

        clientService.activate(verification.getClientId());

        String token = jwtService.generateToken(verification.getClientId());

        return new AuthToken(verification.getClientId(), token, Instant.now());
    }
}