package br.com.ale.application.api;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.application.account.querry.GetAccountDetailsUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.dto.AuthResponse;
import br.com.ale.dto.CreateAccountApiRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountDetailsUseCase getAccountDetailsUseCase;
    private final AuthCookieService authCookieService;

    public AccountController(
            CreateAccountUseCase createAccountUseCase,
            GetAccountDetailsUseCase getAccountDetailsUseCase,
            AuthCookieService authCookieService
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountDetailsUseCase = getAccountDetailsUseCase;
        this.authCookieService = authCookieService;
    }

    @PostMapping
    public AuthResponse create(@RequestBody CreateAccountApiRequest request,
                               HttpServletResponse response) {
        AuthToken authToken = createAccountUseCase.execute(
                new CreateAccountCommand(
                        request.name(),
                        request.email(),
                        request.password()
                )
        );

        authCookieService.addAuthCookie(response, authToken.getToken());

        return new AuthResponse(authToken.getClientId(), "Authenticated");
    }

    @GetMapping("/me")
    public AccountDetailsResponse me(HttpServletRequest request) {
        String token = authCookieService.extractToken(request);
        return getAccountDetailsUseCase.execute(token);
    }
}
