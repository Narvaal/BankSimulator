package br.com.ale.application.api;

import br.com.ale.application.auth.command.LoginCommand;
import br.com.ale.application.auth.usecase.LoginUseCase;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.dto.CreateAuthenticationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public AuthToken login(@RequestBody CreateAuthenticationRequest request) {
        LoginCommand command = new LoginCommand(request.document(), request.password());
        return loginUseCase.execute(command);
    }
}
