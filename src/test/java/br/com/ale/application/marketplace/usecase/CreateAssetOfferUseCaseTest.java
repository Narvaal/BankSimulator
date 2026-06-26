package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
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

class CreateAssetOfferUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetWebhookNotifier webhookNotifier;
    private JwtService jwtService;
    private CreateAssetOfferUseCase useCase;

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

        useCase = new CreateAssetOfferUseCase(assetListingService, accountService, jwtService);

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
    void shouldCreateAssetOfferSuccessfully() {

        Client client = createClient();
        Account owner = createAccount(client);
        AssetUnity unity = createAssetUnity(owner);

        String token = jwtService.generateToken(client.getId());

        AssetListing listing = useCase.execute(
                new CreateAssetOfferCommand(unity.getId(), new BigDecimal("100.00"), token)
        );

        assertNotNull(listing);
        assertEquals(AssetListingStatus.ACTIVE, listing.getStatus());
        assertEquals(owner.getId(), listing.getSellerAccountId());
        assertEquals(unity.getId(), listing.getAssetUnityId());
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        Client client = createClient();
        Account owner = createAccount(client);
        AssetUnity unity = createAssetUnity(owner);

        assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(
                        new CreateAssetOfferCommand(unity.getId(), new BigDecimal("100.00"), "invalid.token.value")
                )
        );
    }

    @Test
    void shouldFailWhenAuthenticatedClientIsNotOwner() {

        Client ownerClient = createClient();
        Client attackerClient = createClient();

        Account owner = createAccount(ownerClient);
        createAccount(attackerClient);

        AssetUnity unity = createAssetUnity(owner);

        String attackerToken = jwtService.generateToken(attackerClient.getId());

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(
                        new CreateAssetOfferCommand(unity.getId(), new BigDecimal("100.00"), attackerToken)
                )
        );
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
}
