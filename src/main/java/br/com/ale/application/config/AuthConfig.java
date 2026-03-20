package br.com.ale.application.config;

import br.com.ale.application.account.querry.GetAccountDetailsUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.application.account.usecase.VerifyAccountUseCase;
import br.com.ale.application.auth.usecase.GoogleLoginUseCase;
import br.com.ale.application.auth.usecase.LocalLoginUseCase;
import br.com.ale.application.client.query.GetClientProfileUseCase;
import br.com.ale.application.transaction.query.ListTransfersByAccountUseCase;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.auth.TokenGenerator;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.infrastructure.db.DefaultConnectionProvider;
import br.com.ale.infrastructure.db.SchemaInitializer;
import br.com.ale.infrastructure.db.secrets.SecretsService;
import br.com.ale.service.ClientService;
import br.com.ale.service.EmailVerificationService;
import br.com.ale.service.TransactionService;
import br.com.ale.service.account.AccountNumberGenerator;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.account.HashAccountNumberGenerator;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.auth.GoogleTokenVerifier;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.crypto.FilePrivateKeyStorage;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.crypto.PrivateKeyStorage;
import br.com.ale.service.email.EmailVerificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;

@Configuration
public class AuthConfig {

    @Bean
    public SecretsService secretsService() {
        return new SecretsService();
    }

    @Bean
    public ConnectionProvider connectionProvider(
            @Value("${db.default.url}") String url,
            @Value("${db.default.user}") String user,
            SecretsService secretsService
    ) {
        String password = secretsService.getDbPassword();

        if (password == null || password.isBlank()) {
            throw new RuntimeException("DB password is null or empty");
        }

        return new DefaultConnectionProvider(url, user, password);
    }

    @Bean
    public SchemaInitializer schemaInitializer(
            ConnectionProvider connectionProvider,
            @Value("${db.schema.auto-create:false}") boolean autoCreate
    ) {
        return new SchemaInitializer(connectionProvider, autoCreate);
    }

    @Bean
    public KeyPairService keyPairService() {
        return new KeyPairService();
    }

    @Bean
    public TokenGenerator tokenGenerator(KeyPairService keyPairService) {
        KeyPair keyPair = keyPairService.generate();
        return new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic());
    }

    @Bean
    public AuthService authService(
            ConnectionProvider connectionProvider,
            TokenGenerator tokenGenerator
    ) {
        return new AuthService(connectionProvider, tokenGenerator);
    }

    @Bean
    public LocalLoginUseCase loginUseCase(ClientService clientService, JwtService jwtService) {
        return new LocalLoginUseCase(clientService, jwtService);
    }

    @Bean
    public PrivateKeyStorage privateKeyStorage(
            @Value("${db.use.test:false}") boolean useTestDb
    ) {
        return useTestDb
                ? new InMemoryPrivateKeyStorage()
                : new FilePrivateKeyStorage();
    }

    @Bean
    public AccountService accountService(
            ConnectionProvider connectionProvider,
            PrivateKeyStorage privateKeyStorage
    ) {
        return new AccountService(connectionProvider, privateKeyStorage);
    }

    @Bean
    public ClientService clientService(ConnectionProvider connectionProvider) {
        return new ClientService(connectionProvider);
    }

    @Bean
    public AccountNumberGenerator accountNumberGenerator() {
        return new HashAccountNumberGenerator();
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(
            AccountService accountService,
            ClientService clientService,
            AccountNumberGenerator accountNumberGenerator,
            EmailVerificationService emailVerificationService,
            EmailVerificationSender emailVerificationSender
    ) {
        return new CreateAccountUseCase(
                accountService,
                clientService,
                accountNumberGenerator,
                emailVerificationService,
                emailVerificationSender
        );
    }

    @Bean
    public GoogleLoginUseCase googleLoginUseCase(
            AccountNumberGenerator accountNumberGenerator,
            AccountService accountService,
            ClientService clientService,
            JwtService jwtService,
            GoogleTokenVerifier googleTokenVerifier,
            @Value("${google.client-id}") String googleClientId
    ) {
        return new GoogleLoginUseCase(
                accountNumberGenerator,
                accountService,
                clientService,
                jwtService,
                googleTokenVerifier,
                googleClientId
        );
    }

    @Bean
    public VerifyAccountUseCase verifyAccountUseCase(
            EmailVerificationService emailVerificationService,
            ClientService clientService,
            JwtService jwtService
    ) {
        return new VerifyAccountUseCase(
                emailVerificationService,
                clientService,
                jwtService
        );
    }

    @Bean
    public DepositAccountUseCase depositAccountUseCase(
            AccountService accountService,
            ClientService clientService
    ) {
        return new DepositAccountUseCase(accountService, clientService);
    }

    @Bean
    public GetAccountDetailsUseCase getAccountDetailsUseCase(
            AccountService accountService,
            JwtService jwtService
    ) {
        return new GetAccountDetailsUseCase(accountService, jwtService);
    }

    @Bean
    public GetClientProfileUseCase getClientProfileUseCase(
            ClientService clientService,
            AuthService authService
    ) {
        return new GetClientProfileUseCase(clientService, authService);
    }

    @Bean
    public TransactionService transactionService(ConnectionProvider connectionProvider) {
        return new TransactionService(connectionProvider);
    }

    @Bean
    public ListTransfersByAccountUseCase listTransfersByAccountUseCase(
            TransactionService transactionService,
            AccountService accountService,
            AuthService authService
    ) {
        return new ListTransfersByAccountUseCase(
                transactionService,
                accountService,
                authService
        );
    }
}