package br.com.ale.application.api;

import br.com.ale.application.account.command.ChangePasswordCommand;
import br.com.ale.application.account.command.ChangePasswordSenderCommand;
import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.application.account.querry.GetAccountDetailsUseCase;
import br.com.ale.application.account.usecase.ChangePasswordUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.application.account.usecase.RequestPasswordResetUseCase;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AuthCookieService authCookieService;
    private final CreateAccountUseCase createAccountUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetAccountDetailsUseCase getAccountDetailsUseCase;

    public AccountController(
            AuthCookieService authCookieService,
            CreateAccountUseCase createAccountUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            GetAccountDetailsUseCase getAccountDetailsUseCase
    ) {
        this.authCookieService = authCookieService;
        this.createAccountUseCase = createAccountUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.getAccountDetailsUseCase = getAccountDetailsUseCase;
    }

    @PostMapping
    public CreateAccountResponse create(@RequestBody CreateAccountApiRequest request) {

        createAccountUseCase.execute(
                new CreateAccountCommand(
                        request.name(),
                        request.email(),
                        request.password()
                )
        );

        return new CreateAccountResponse(
                "Account created. Check your email to activate your account."
        );
    }

    @GetMapping("/me")
    public AccountDetailsResponse me(HttpServletRequest request) {
        String token = authCookieService.extractToken(request);
        return getAccountDetailsUseCase.execute(token);
    }

    @PostMapping("/password/reset-request")
    public void requestReset(@RequestBody EmailRequest request) {
        requestPasswordResetUseCase.execute(
                new ChangePasswordSenderCommand(request.email())
        );
    }

    @PostMapping("/password/reset")
    public void resetPassword(@RequestBody CreateResetPasswordRequest request) {
        changePasswordUseCase.execute(
                new ChangePasswordCommand(request.password(), request.token())
        );
    }
}
