package br.com.ale.application.client.query;

import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.client.Client;
import br.com.ale.dto.ClientProfileResponse;
import br.com.ale.service.ClientService;
import br.com.ale.service.auth.AuthService;

public class GetClientProfileUseCase {

    private final ClientService clientService;
    private final AuthService authService;

    public GetClientProfileUseCase(ClientService clientService, AuthService authService) {
        this.clientService = clientService;
        this.authService = authService;
    }

    public ClientProfileResponse execute(String token) {
        TokenClaims claims = authService.validateToken(token);
        Client client = clientService.getClientById(claims.clientId());
        return new ClientProfileResponse(client.getName(), client.getEmail());
    }
}
