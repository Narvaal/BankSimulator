package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CancelAssetOfferUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetWebhookNotifier webhookNotifier;
    private JwtService jwtService;
    private CancelAssetOfferUseCase useCase;

    @BeforeEach
    void setup() {

        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);
        jwtService = createTestJwtService();

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);

        useCase = new CancelAssetOfferUseCase(assetListingService, jwtService);

        cleanDatabase();
    }

    private JwtService createTestJwtService() {
        JwtService service = new JwtService();
        String secret = Base64.getEncoder().encodeToString(
                "test-secret-key-for-unit-tests!!".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);
        return service;
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
        Account owner = createAccount(client);

        AssetUnity unity = createAssetUnity(owner);
        AssetListing listing = createActiveListing(unity, owner);

        String token = jwtService.generateToken(client.getId());

        assertDoesNotThrow(() -> useCase.execute(new CancelAssetCommand(listing.getId(), token)));

        AssetListing updated = assetListingService.selectById(listing.getId());
        assertEquals(AssetListingStatus.CANCELED, updated.getStatus());
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        Client client = createClient();
        Account owner = createAccount(client);

        AssetUnity unity = createAssetUnity(owner);
        AssetListing listing = createActiveListing(unity, owner);

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(new CancelAssetCommand(listing.getId(), "invalid.token.value"))
        );
    }

    @Test
    void shouldFailWhenAuthenticatedClientIsNotOwner() {

        Client ownerClient = createClient();
        Client attackerClient = createClient();

        Account owner = createAccount(ownerClient);
        createAccount(attackerClient);

        AssetUnity unity = createAssetUnity(owner);
        AssetListing listing = createActiveListing(unity, owner);

        String attackerToken = jwtService.generateToken(attackerClient.getId());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> useCase.execute(new CancelAssetCommand(listing.getId(), attackerToken))
        );

        assertInstanceOf(UnauthorizedOperationException.class, ex.getCause());
    }

    @Test
    void shouldFailWhenListingIsNotActive() {

        Client client = createClient();
        Account owner = createAccount(client);

        AssetUnity unity = createAssetUnity(owner);
        AssetListing listing = createActiveListing(unity, owner);

        assetListingService.updateStatus(listing.getId(), AssetListingStatus.CANCELED);

        String token = jwtService.generateToken(client.getId());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> useCase.execute(new CancelAssetCommand(listing.getId(), token))
        );

        assertInstanceOf(InvalidAssetListingStateException.class, ex.getCause());
    }

    private Client createClient() {
        return clientService.createClient(
                new CreateClientRequest(
                        "Client " + System.nanoTime(),
                        "email-" + System.nanoTime() + "@test.com",
                        "pass",
                        Provider.LOCAL,
                        null,
                        false,
                        null
                )
        );
    }

    private Account createAccount(Client client) {
        return accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        "ACC-" + System.nanoTime(),
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );
    }

    private AssetUnity createAssetUnity(Account owner) {

        Asset asset = assetService.createAsset(
                new CreateAssetRequest("Asset " + System.nanoTime(), 1)
        );

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(asset.getId(), owner.getId())
        );
    }

    private AssetListing createActiveListing(AssetUnity unity, Account owner) {
        return assetListingService.createAssetOffer(
                new CreateAssetListingRequest(
                        unity.getId(),
                        owner.getId(),
                        new BigDecimal("100.00"),
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
