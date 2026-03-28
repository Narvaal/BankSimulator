package br.com.ale.application.api;

import br.com.ale.application.transaction.query.ListTransfersByAccountUseCase;
import br.com.ale.domain.transaction.Transaction;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountTransfersController {

    private final ListTransfersByAccountUseCase listTransfersByAccountUseCase;

    public AccountTransfersController(ListTransfersByAccountUseCase listTransfersByAccountUseCase) {
        this.listTransfersByAccountUseCase = listTransfersByAccountUseCase;
    }

    @GetMapping("/{id}/transfers")
    public List<Transaction> listTransfers(
            @PathVariable("id") long accountId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        String token = extractToken(authorization, tokenParam);
        return listTransfersByAccountUseCase.execute(accountId, token);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
