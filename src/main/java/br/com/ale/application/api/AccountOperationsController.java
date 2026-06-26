package br.com.ale.application.api;

import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.domain.account.Account;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class AccountOperationsController {

    private final DepositAccountUseCase depositAccountUseCase;
    private final AccountService accountService;
    private final JwtService jwtService;

    public AccountOperationsController(DepositAccountUseCase depositAccountUseCase,
                                       AccountService accountService,
                                       JwtService jwtService) {
        this.depositAccountUseCase = depositAccountUseCase;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    @PostMapping("/admin/accounts/deposit")
    public ResponseEntity<?> deposit(@RequestBody AdminDepositRequest body) {
        long clientId;

        if (body.clientId() != null) {
            clientId = body.clientId();
        } else if (body.token() != null) {
            clientId = jwtService.extractClientId(body.token());
        } else {
            return ResponseEntity.badRequest().body("Provide clientId or token");
        }

        Account account = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Account not found for clientId=" + clientId));

        accountService.credit(account.getAccountNumber(), new BigDecimal(body.amount()));

        return ResponseEntity.ok("Balance updated");
    }

    record AdminDepositRequest(Long clientId, String token, String amount) {}
}
