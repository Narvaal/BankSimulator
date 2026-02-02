package br.com.ale.application.auth.usecase;

import br.com.ale.application.auth.command.LoginCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.service.auth.AuthService;

public class LoginUseCase {

    private final AuthService authService;

    public LoginUseCase(AuthService authService) {
        this.authService = authService;
    }

    public AuthToken execute(LoginCommand command) {
        return authService.authenticate(
                new CreateAuthenticationRequest(
                        command.email(),
                        command.password()
                )
        );
    }
}
