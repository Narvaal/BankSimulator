package br.com.ale.application.auth.usecase;

import br.com.ale.application.auth.command.LocalLoginCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Client;
import br.com.ale.dto.CreateAuthenticationRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;
import java.util.Optional;

public class LocalLoginUseCase {

    private final ClientService clientService;
    private final JwtService jwtService;

    public LocalLoginUseCase(
            ClientService clientService,
            JwtService jwtService

    ) {
        this.clientService = clientService;
        this.jwtService = jwtService;
    }

    public AuthToken execute(LocalLoginCommand command) {

        Client client = clientService.getClientByEmail(command.email());

        if (!PasswordHasher.matches(command.password(), client.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String jwt = jwtService.generateToken(client.getId());

        return new AuthToken(
                client.getId(),
                jwt,
                Instant.now()
        );
    }
}
