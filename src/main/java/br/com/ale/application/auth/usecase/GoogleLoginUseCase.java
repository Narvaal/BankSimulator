package br.com.ale.application.auth.usecase;

import br.com.ale.application.auth.command.GoogleLoginCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountNumberGenerator;
import br.com.ale.service.auth.GoogleTokenVerifier;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class GoogleLoginUseCase {

    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountService accountService;
    private final ClientService clientService;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final String googleClientId;

    public GoogleLoginUseCase(
            AccountNumberGenerator accountNumberGenerator,
            AccountService accountService,
            ClientService clientService,
            JwtService jwtService,
            GoogleTokenVerifier googleTokenVerifier,
            String googleClientId
    ){
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountService = accountService;
        this.clientService = clientService;
        this.jwtService = jwtService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.googleClientId = googleClientId;
    }

    private Client findOrCreateGoogleClient(
            String name,
            String email,
            String googleId,
            boolean emailVerified,
            String picture
    ) {

        Optional<Client> existing =
                clientService.getClientByProviderAndId(Provider.GOOGLE, googleId);

        return existing.orElseGet(() -> clientService.createClient(new CreateClientRequest(
                name,
                email,
                null,
                Provider.GOOGLE,
                googleId,
                emailVerified,
                picture
        )));

    }

    private void findOrCreateAccount(Client client) {

        Optional<Account> existing = accountService.getAccountByClientId(client.getId());

        existing.orElseGet(() -> accountService.createAccount(new CreateAccountRequest(
                client.getId(),
                accountNumberGenerator.generate(client),
                AccountType.DEFAULT,
                AccountStatus.ACTIVE
        )));
    }

    public AuthToken execute(GoogleLoginCommand command) {

        Map<String, Object> payload = googleTokenVerifier.verify(command.idToken());

        if (payload == null || payload.isEmpty()) {
            throw new SecurityException("Invalid Google token");
        }

        String googleId = (String) payload.get("sub");
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean emailVerified = Boolean.parseBoolean(
                String.valueOf(payload.get("email_verified"))
        );

        if (!emailVerified) {
            throw new SecurityException("Google email not verified");
        }

        String aud = (String) payload.get("aud");
        if (!googleClientId.equals(aud)) {
            throw new SecurityException("Invalid Google token audience");
        }

        Client client = findOrCreateGoogleClient(name, email, googleId, emailVerified, picture);
        findOrCreateAccount(client);

        String jwt = jwtService.generateToken(client.getId());

        return new AuthToken(
                client.getId(),
                jwt,
                Instant.now()
        );
    }
}
