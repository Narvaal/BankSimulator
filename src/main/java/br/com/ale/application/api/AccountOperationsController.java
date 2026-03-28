package br.com.ale.application.api;

import br.com.ale.application.account.usecase.DepositAccountUseCase;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountOperationsController {

    private final DepositAccountUseCase depositAccountUseCase;

    public AccountOperationsController(DepositAccountUseCase depositAccountUseCase) {
        this.depositAccountUseCase = depositAccountUseCase;
    }

}
