package br.com.ale.application.api;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.dto.DepositAccountApiRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountOperationsController {

    private final DepositAccountUseCase depositAccountUseCase;

    public AccountOperationsController(DepositAccountUseCase depositAccountUseCase) {
        this.depositAccountUseCase = depositAccountUseCase;
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable("id") long accountId,
            @RequestBody DepositAccountApiRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractToken(authorization, request.token());
        DepositAccountCommand command =
                new DepositAccountCommand(accountId, request.amount(), token);
        depositAccountUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
