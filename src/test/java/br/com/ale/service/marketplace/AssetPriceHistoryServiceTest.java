package br.com.ale.service.marketplace;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.PrivateKeyStorage;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssetPriceHistoryServiceTest {

    private TestConnectionProvider provider;
    private ClientService clientService;
    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetPriceHistoryService assetPriceHistoryService;
    private AssetWebhookNotifier webhookNotifier;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);
        PrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        clientService = new ClientService(provider);
        accountService = new AccountService(provider, privateKeyStorage);
        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);
        assetPriceHistoryService = new AssetPriceHistoryService(provider);
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
    void shouldRegisterPriceChangeSuccessfully() {

        AssetListing listing = createListing(new BigDecimal("100.00"));
        Account admin = createAccount();

        assertDoesNotThrow(() ->
                assetPriceHistoryService.registerPriceChange(
                        listing.getId(),
                        new BigDecimal("120.00"),
                        admin.getId(),
                        ReasonType.MANUAL_ADJUSTMENT
                )
        );
    }

    @Test
    void shouldFailWhenListingDoesNotExist() {

        Account admin = createAccount();

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> assetPriceHistoryService.registerPriceChange(
                                9999L,
                                new BigDecimal("120.00"),
                                admin.getId(),
                                ReasonType.MANUAL_ADJUSTMENT
                        )
                );

        assertNotNull(ex.getCause());
        assertTrue(
                ex.getCause().getMessage().contains("AssetListing not found"),
                ex.getCause().getMessage()
        );
    }

    private Account createAccount() {

        var client =
                clientService.createClient(
                        new CreateClientRequest(
                                "Client " + System.nanoTime(),
                                String.valueOf(System.nanoTime()),
                                "pass",
                                Provider.LOCAL,
                                null,
                                false,
                                null
                        )
                );

        return accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        "ACC-" + System.nanoTime(),
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );
    }

    private AssetListing createListing(BigDecimal price) {

        Account seller = createAccount();

        Asset asset =
                assetService.createAsset(
                        new CreateAssetRequest(
                                "Asset " + System.nanoTime(),
                                1
                        )
                );

        AssetUnity unity =
                assetUnityService.createAssetUnity(
                        new CreateAssetUnityRequest(
                                asset.getId(),
                                seller.getId()
                        )
                );

        return assetListingService.createAssetOffer(
                new CreateAssetListingRequest(
                        unity.getId(),
                        seller.getId(),
                        price,
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
