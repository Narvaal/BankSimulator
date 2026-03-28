package br.com.ale.application.api;

import br.com.ale.application.account.command.DepositAccountCommand;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.dto.KofiWebHookResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kofi")
public class KofiWebhookController {

    private final DepositAccountUseCase depositAccountUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kofi.verification-token}")
    private String verificationToken;


    public KofiWebhookController(DepositAccountUseCase depositAccountUseCase) {
        this.depositAccountUseCase = depositAccountUseCase;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestParam("data") String data) {

        try {

            KofiWebHookResponse response = objectMapper.readValue(data, KofiWebHookResponse.class);

            if (!response.token().equals(verificationToken)) {
                throw new IllegalAccessError("Invalid Token");
            }

            depositAccountUseCase.execute(new DepositAccountCommand(response.email(), response.amount()));

            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error");
        }
    }
}