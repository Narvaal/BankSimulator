package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CancelAssetOfferUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;

    private AuthService authService;
    private CancelAssetOfferUseCase useCase;

    @BeforeEach
    void setup() {

        provider = new TestConnectionProvider();

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider);
        assetListingService = new AssetListingService(provider);

        authService = new AuthService(provider);

        cleanDatabase();
    }


    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset_price_history");
            stmt.execute("DELETE FROM asset_transfer");
            stmt.execute("DELETE FROM asset_listing");
            stmt.execute("DELETE FROM asset_unit");
            stmt.execute("DELETE FROM asset");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCancelOfferSuccessfully() {

        Client client = createClient();
        Account owner = createAccountWithCredential(client);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                owner.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CancelAssetOfferUseCase(
                accountService,
                assetListingService,
                assetUnityService,
                authService
        );

        AssetUnity unity = createAssetUnity(owner);

        AuthToken token =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                client.getDocument(),
                                "password"
                        )
                );

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                owner.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        CancelAssetCommand command =
                new CancelAssetCommand(
                        owner.getId(),
                        listing.getId(),
                        token.getToken()
                );

        assertDoesNotThrow(() -> useCase.execute(command));

        AssetListing updated =
                assetListingService.selectById(listing.getId());

        assertEquals(AssetListingStatus.CANCELED, updated.getStatus());
    }

    private Account createAccountWithCredential(Client client) {

        Account account =
                accountService.createAccount(
                        new CreateAccountRequest(
                                client.getId(),
                                "ACC-" + System.nanoTime(),
                                AccountType.DEFAULT,
                                AccountStatus.ACTIVE
                        )
                );

        authService.register(
                new CreateCredentialRequest(
                        client.getDocument(),
                        "password"
                )
        );

        return account;
    }

    private AssetUnity createAssetUnity(Account owner) {

        Asset asset =
                assetService.createAsset(
                        new CreateAssetRequest(
                                "Asset " + System.nanoTime(),
                                1
                        )
                );

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(
                        asset.getId(),
                        owner.getId()
                )
        );
    }

    private Client createClient() {
        return clientService.createClient(
                new CreateClientRequest(
                        "Client " + System.nanoTime(),
                        String.valueOf(System.nanoTime())
                )
        );
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        Client client = createClient();
        Account owner = createAccountWithCredential(client);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                owner.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CancelAssetOfferUseCase(
                accountService,
                assetListingService,
                assetUnityService,
                authService
        );

        AssetUnity unity = createAssetUnity(owner);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                owner.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        CancelAssetCommand command =
                new CancelAssetCommand(
                        owner.getId(),
                        listing.getId(),
                        "invalid.token.value"
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(command)
        );
    }

    @Test
    void shouldFailWhenAuthenticatedClientIsNotOwner() {

        Client ownerClient = createClient();
        Client attackerClient = createClient();

        Account owner = createAccountWithCredential(ownerClient);
        Account attacker = createAccountWithCredential(attackerClient);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                attacker.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CancelAssetOfferUseCase(
                accountService,
                assetListingService,
                assetUnityService,
                authService
        );

        AssetUnity unity = createAssetUnity(owner);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                owner.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        AuthToken attackerToken =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                attackerClient.getDocument(),
                                "password"
                        )
                );

        CancelAssetCommand command =
                new CancelAssetCommand(
                        attacker.getId(),
                        listing.getId(),
                        attackerToken.getToken()
                );

        assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(command)
        );
    }

    @Test
    void shouldFailWhenListingIsNotActive() {

        Client client = createClient();
        Account owner = createAccountWithCredential(client);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                owner.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CancelAssetOfferUseCase(
                accountService,
                assetListingService,
                assetUnityService,
                authService
        );

        AssetUnity unity = createAssetUnity(owner);

        AuthToken token =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                client.getDocument(),
                                "password"
                        )
                );

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                owner.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        // muda o estado para CANCELED
        assetListingService.updateStatus(
                listing.getId(),
                AssetListingStatus.CANCELED
        );

        CancelAssetCommand command =
                new CancelAssetCommand(
                        owner.getId(),
                        listing.getId(),
                        token.getToken()
                );

        assertThrows(
                InvalidAssetListingStateException.class,
                () -> useCase.execute(command)
        );
    }
}
