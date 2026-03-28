package br.com.ale.application.api;

import br.com.ale.application.client.query.GetClientProfileUseCase;
import br.com.ale.dto.ClientProfileResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final GetClientProfileUseCase getClientProfileUseCase;

    public ClientController(GetClientProfileUseCase getClientProfileUseCase) {
        this.getClientProfileUseCase = getClientProfileUseCase;
    }

    @GetMapping("/me")
    public ClientProfileResponse me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        String token = extractToken(authorization, tokenParam);
        return getClientProfileUseCase.execute(token);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
