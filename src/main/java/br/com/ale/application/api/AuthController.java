package br.com.ale.application.api;

import br.com.ale.application.account.command.ResendVerificationCommand;
import br.com.ale.application.account.command.VerifyAccountCommand;
import br.com.ale.application.account.usecase.ResendVerificationUseCase;
import br.com.ale.application.account.usecase.VerifyAccountUseCase;
import br.com.ale.application.auth.command.GoogleLoginCommand;
import br.com.ale.application.auth.command.LocalLoginCommand;
import br.com.ale.application.auth.usecase.GoogleLoginUseCase;
import br.com.ale.application.auth.usecase.LocalLoginUseCase;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.dto.AuthResponse;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.dto.CreateGoogleAuthenticationRequest;
import br.com.ale.dto.ResendVerificationRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LocalLoginUseCase localLoginUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final AuthCookieService authCookieService;
    private final VerifyAccountUseCase verifyAccountUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;

    public AuthController(LocalLoginUseCase localLoginUseCase,
                          GoogleLoginUseCase googleLoginUseCase,
                          AuthCookieService authCookieService,
                          VerifyAccountUseCase verifyAccountUseCase,
                          ResendVerificationUseCase resendVerificationUseCase

    ) {
        this.localLoginUseCase = localLoginUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.authCookieService = authCookieService;
        this.verifyAccountUseCase = verifyAccountUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
    }

    @GetMapping("/verify")
    public void verifyEmail(@RequestParam("token") String token,
                            HttpServletResponse response) throws IOException {

        AuthToken authToken = verifyAccountUseCase.execute(
                new VerifyAccountCommand(token)
        );

        authCookieService.addAuthCookie(response, authToken.getToken());

        response.sendRedirect("https://app.alessandro-bezerra.me/inventory");
    }

    @PostMapping("/resend-verification")
    public void resend(@RequestBody ResendVerificationRequest request) {
        resendVerificationUseCase.execute(
                new ResendVerificationCommand(
                        request.email()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody CreateAuthenticationRequest request,
            HttpServletResponse response
    ) {

        try {

            AuthToken authToken = localLoginUseCase.execute(
                    new LocalLoginCommand(
                            request.email(),
                            request.password()
                    )
            );

            authCookieService.addAuthCookie(response, authToken.getToken());

            return ResponseEntity.ok(
                    new AuthResponse(authToken.getClientId(), "Authenticated")
            );

        } catch (IllegalArgumentException e) {

            String message = e.getMessage();

            if ("Email not verified".equals(message)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "code", "EMAIL_NOT_VERIFIED",
                                "message", message
                        ));
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "code", "INVALID_CREDENTIALS",
                            "message", message
                    ));
        }
    }

    @PostMapping("/google")
    public AuthResponse login(@RequestBody CreateGoogleAuthenticationRequest request,
                              HttpServletResponse response) throws IOException {

        AuthToken authToken = googleLoginUseCase.execute(
                new GoogleLoginCommand(request.token())
        );

        authCookieService.addAuthCookie(response, authToken.getToken());

        return new AuthResponse(authToken.getClientId(), "Authenticated");
    }
}
