package br.com.ale.application.config;

import br.com.ale.application.auth.usecase.LoginUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.application.account.usecase.GetAccountDetailsUseCase;
import br.com.ale.application.client.query.GetClientProfileUseCase;
import br.com.ale.application.transaction.query.ListTransfersByAccountUseCase;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.auth.TokenGenerator;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.infrastructure.db.DefaultConnectionProvider;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.AccountService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.TransactionService;
import br.com.ale.service.ClientService;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.account.AccountNumberGenerator;
import br.com.ale.service.account.HashAccountNumberGenerator;
import br.com.ale.service.crypto.FilePrivateKeyStorage;
import br.com.ale.service.crypto.PrivateKeyStorage;
import java.security.KeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    @Bean
    public ConnectionProvider connectionProvider(
            @Value("${db.default.url}") String url,
            @Value("${db.default.user}") String user,
            @Value("${db.default.password:}") String password,
            @Value("${db.use.test:false}") boolean useTestDb
    ) {
        if (useTestDb) {
            return new TestConnectionProvider();
        }
        return new DefaultConnectionProvider(url, user, password);
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
    public LoginUseCase loginUseCase(AuthService authService) {
        return new LoginUseCase(authService);
    }

    @Bean
    public PrivateKeyStorage privateKeyStorage() {
        return new FilePrivateKeyStorage();
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
            AuthService authService
    ) {
        return new CreateAccountUseCase(
                accountService,
                clientService,
                accountNumberGenerator,
                authService
        );
    }

    @Bean
    public DepositAccountUseCase depositAccountUseCase(
            AccountService accountService,
            AuthService authService
    ) {
        return new DepositAccountUseCase(accountService, authService);
    }

    @Bean
    public GetAccountDetailsUseCase getAccountDetailsUseCase(
            AccountService accountService,
            AuthService authService
    ) {
        return new GetAccountDetailsUseCase(accountService, authService);
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
