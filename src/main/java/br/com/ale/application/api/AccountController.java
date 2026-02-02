package br.com.ale.application.api;

import br.com.ale.application.account.command.CreateAccountCommand;
import br.com.ale.application.account.usecase.GetAccountDetailsUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.domain.account.Account;
import br.com.ale.dto.AccountDetailsResponse;
import br.com.ale.dto.CreateAccountApiRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountDetailsUseCase getAccountDetailsUseCase;

    public AccountController(
            CreateAccountUseCase createAccountUseCase,
            GetAccountDetailsUseCase getAccountDetailsUseCase
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountDetailsUseCase = getAccountDetailsUseCase;
    }

    @PostMapping
    public Account create(@RequestBody CreateAccountApiRequest request) {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        request.name(),
                        request.email(),
                        request.password()
                );
        return createAccountUseCase.execute(command);
    }

    @GetMapping("/{id}")
    public AccountDetailsResponse getById(
            @PathVariable("id") long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        String token = extractToken(authorization, tokenParam);
        return getAccountDetailsUseCase.execute(id, token);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
