package br.com.ale.application.account.usecase;

import br.com.ale.application.account.command.ChangePasswordCommand;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.auth.PasswordValidator;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;

public class ChangePasswordUseCase {
    private final ClientService clientService;
    private final EmailVerificationService emailVerificationService;

    public ChangePasswordUseCase(ClientService clientService,
                                 EmailVerificationService emailVerificationService) {
        this.clientService = clientService;
        this.emailVerificationService = emailVerificationService;
    }

    public void execute(ChangePasswordCommand command) {

        EmailVerification verification = emailVerificationService.confirmToken(
                command.token(), EmailVerificationType.PASSWORD_RESET
        );

        PasswordValidator.validate(command.password());

        clientService.updateClient(new UpdateClientRequest(verification.getClientId(),
                PasswordHasher.hash(command.password()))
        );
    }
}
