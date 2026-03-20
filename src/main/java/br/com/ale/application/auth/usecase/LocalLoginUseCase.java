package br.com.ale.application.auth.usecase;

import br.com.ale.application.auth.command.LocalLoginCommand;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Client;
import br.com.ale.service.ClientService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

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

        Client client;

        try {
            client = clientService.getClientByEmail(command.email());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!PasswordHasher.matches(command.password(), client.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!client.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified");
        }

        String jwt = jwtService.generateToken(client.getId());

        return new AuthToken(
                client.getId(),
                jwt,
                Instant.now()
        );
    }
}