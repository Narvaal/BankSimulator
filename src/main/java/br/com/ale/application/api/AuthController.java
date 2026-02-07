package br.com.ale.application.api;

import br.com.ale.application.auth.command.GoogleLoginCommand;
import br.com.ale.application.auth.command.LocalLoginCommand;
import br.com.ale.application.auth.usecase.GoogleLoginUseCase;
import br.com.ale.application.auth.usecase.LocalLoginUseCase;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.dto.AuthResponse;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateGoogleAuthenticationRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LocalLoginUseCase localLoginUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final AuthCookieService authCookieService;


    public AuthController(LocalLoginUseCase localLoginUseCase, GoogleLoginUseCase googleLoginUseCase,
                          AuthCookieService authCookieService) {
        this.localLoginUseCase = localLoginUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody CreateAuthenticationRequest request,
                              HttpServletResponse response) {

        AuthToken authToken = localLoginUseCase.execute(
                new LocalLoginCommand(
                        request.email(),
                        request.password()
                )
        );

        authCookieService.addAuthCookie(response, authToken.getToken());

        return new AuthResponse(authToken.getClientId(), "Authenticated");
    }

    @PostMapping("/google")
    public AuthResponse login(@RequestBody CreateGoogleAuthenticationRequest request,
                              HttpServletResponse response) {

        AuthToken authToken = googleLoginUseCase.execute(
          new GoogleLoginCommand(request.token())
        );

        authCookieService.addAuthCookie(response, authToken.getToken());

        return new AuthResponse(authToken.getClientId(), "Authenticated");
    }
}
