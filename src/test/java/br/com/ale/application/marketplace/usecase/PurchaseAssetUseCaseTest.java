package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.PasswordHasher;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.auth.SimpleTokenGenerator;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.KeyPairService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseAssetUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetWebhookNotifier webhookNotifier;

    private AssetPurchaseService assetPurchaseService;
    private AssetPriceHistoryService assetPriceHistoryService;

    private AuthService authService;
    private PurchaseAssetUseCase useCase;

    @BeforeEach
    void setup() {

        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);

        assetPurchaseService = new AssetPurchaseService(provider, webhookNotifier);
        assetPriceHistoryService = new AssetPriceHistoryService(provider);

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
    void shouldPurchaseAssetSuccessfully() {

        Client sellerClient = createClient();
        Client buyerClient = createClient();

        Account seller = createAccountWithCredential(sellerClient);
        Account buyer = createAccountWithCredential(buyerClient);
        accountService.credit(buyer.getAccountNumber(), new BigDecimal("100.00"));

        var keyPair = new KeyPairService().generate();

        InMemoryPrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        privateKeyStorage.save(buyer.getId(), keyPair.getPrivate().getEncoded());

        authService = new AuthService(
                provider,
                new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic())
        );

        useCase = new PurchaseAssetUseCase(
                accountService,
                assetListingService,
                assetPurchaseService,
                assetPriceHistoryService,
                authService
        );

        AssetUnity unity = createAssetUnity(seller);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),         
                                seller.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        AuthToken buyerToken =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                buyerClient.getEmail(),
                                "password"
                        )
                );

        AssetPurchase purchase =
                useCase.execute(
                        new PurchaseAssetCommand(
                                buyer.getId(),
                                listing.getId(),
                                buyerToken.getToken()
                        )
                );

        assertNotNull(purchase);

        AssetListing updatedListing =
                assetListingService.selectById(listing.getId());

        assertEquals(AssetListingStatus.SOLD, updatedListing.getStatus());
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        Client sellerClient = createClient();
        Client buyerClient = createClient();

        Account seller = createAccountWithCredential(sellerClient);
        createAccountWithCredential(buyerClient);

        var keyPair = new KeyPairService().generate();

        authService = new AuthService(
                provider,
                new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic())
        );

        useCase = new PurchaseAssetUseCase(
                accountService,
                assetListingService,
                assetPurchaseService,
                assetPriceHistoryService,
                authService
        );

        AssetUnity unity = createAssetUnity(seller);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                seller.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(
                        new PurchaseAssetCommand(
                                999L,
                                listing.getId(),
                                "token.invalido"
                        )
                )
        );
    }

    @Test
    void shouldFailWhenAuthenticatedClientIsNotBuyer() {

        Client sellerClient = createClient();
        Client buyerClient = createClient();
        Client attackerClient = createClient();

        Account seller = createAccountWithCredential(sellerClient);
        Account buyer = createAccountWithCredential(buyerClient);
        Account attacker = createAccountWithCredential(attackerClient);

        var keyPair = new KeyPairService().generate();

        InMemoryPrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        privateKeyStorage.save(attacker.getId(), keyPair.getPrivate().getEncoded());

        authService = new AuthService(
                provider,
                new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic())
        );

        useCase = new PurchaseAssetUseCase(
                accountService,
                assetListingService,
                assetPurchaseService,
                assetPriceHistoryService,
                authService
        );

        AssetUnity unity = createAssetUnity(seller);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                seller.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                );

        AuthToken attackerToken =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                attackerClient.getEmail(),
                                "password"
                        )
                );

        assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(
                        new PurchaseAssetCommand(
                                buyer.getId(),
                                listing.getId(),
                                attackerToken.getToken()
                        )
                )
        );
    }

    @Test
    void shouldFailWhenListingIsNotActive() {

        Client sellerClient = createClient();
        Client buyerClient = createClient();

        Account seller = createAccountWithCredential(sellerClient);
        Account buyer = createAccountWithCredential(buyerClient);

        var keyPair = new KeyPairService().generate();

        InMemoryPrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        privateKeyStorage.save(buyer.getId(), keyPair.getPrivate().getEncoded());

        authService = new AuthService(
                provider,
                new SimpleTokenGenerator(keyPair.getPrivate(), keyPair.getPublic())
        );

        useCase = new PurchaseAssetUseCase(
                accountService,
                assetListingService,
                assetPurchaseService,
                assetPriceHistoryService,
                authService
        );

        AssetUnity unity = createAssetUnity(seller);

        AssetListing listing =
                assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                unity.getId(),
                                seller.getId(),
                                new BigDecimal("100.00"),
                                AssetListingStatus.CANCELED
                        )
                );

        AuthToken buyerToken =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                buyerClient.getEmail(),
                                "password"
                        )
                );

        assertThrows(
                InvalidAssetListingStateException.class,
                () -> useCase.execute(
                        new PurchaseAssetCommand(
                                buyer.getId(),
                                listing.getId(),
                                buyerToken.getToken()
                        )
                )
        );
    }

    // ---------- helpers ----------

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

        String hashed = PasswordHasher.hash("password");

        return clientService.createClient(
                new CreateClientRequest(
                        "Client " + System.nanoTime(),
                        String.valueOf(System.nanoTime()),
                        hashed,
                        Provider.LOCAL,
                        null,
                        false,
                        null
                )
        );
    }
}
