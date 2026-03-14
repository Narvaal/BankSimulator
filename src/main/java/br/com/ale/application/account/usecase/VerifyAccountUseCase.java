package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.VerifyAccountCommand;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.service.EmailVerificationService;

public class VerifyAccountUseCase {

    private final EmailVerificationService emailVerificationService;

    public VerifyAccountUseCase(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    public void execute(VerifyAccountCommand command) {

        EmailVerification emailVerification =
                emailVerificationService.validateToken(command.token());

        emailVerificationService.markVerified(emailVerification.getId());
    }
}