package br.com.ale.service.marketplace;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.client.Client;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssetPurchaseServiceTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetPurchaseService assetPurchaseService;
    private AssetWebhookNotifier webhookNotifier;

    private Account seller;
    private Account buyer;

    @BeforeEach
    void setup() {
        InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);

        assetPurchaseService =
                new AssetPurchaseService(
                        provider,
                        webhookNotifier
                );

        cleanDatabase();

        seller = createAccount();
        buyer = createAccount();
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

    private Account createAccount() {

        Client client =
                clientService.createClient(
                        new CreateClientRequest(
                                "Client-" + System.nanoTime(),
                                "DOC-" + System.nanoTime()
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

    private Asset createAsset() {
        return assetService.createAsset(
                new CreateAssetRequest(
                        "Cool Asset " + System.nanoTime(),
                        1
                )
        );
    }

    private AssetUnity createUnity(Asset asset, Account owner) {
        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(
                        asset.getId(),
                        owner.getId()
                )
        );
    }

    private AssetListing createListing(AssetUnity unity, AssetListingStatus status) {
        return assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity.getId(),
                        seller.getId(),
                        new BigDecimal("100.00"),
                        status
                )
        );
    }

    @Test
    void shouldPurchaseAssetSuccessfully() {

        Asset asset = createAsset();
        AssetUnity unity = createUnity(asset, seller);
        AssetListing listing = createListing(unity, AssetListingStatus.ACTIVE);

        AssetPurchase purchase =
                assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                listing.getId(),
                                buyer.getId()
                        )
                );

        assertNotNull(purchase);
        assertEquals(listing.getId(), purchase.getListingId());
        assertEquals(unity.getId(), purchase.getAssetUnityId());
        assertEquals(seller.getId(), purchase.getSellerAccountId());
        assertEquals(buyer.getId(), purchase.getBuyerAccountId());
        assertEquals(new BigDecimal("100.00"), purchase.getPrice());

        AssetListing reloaded =
                assetListingService.selectById(listing.getId());

        assertEquals(AssetListingStatus.SOLD, reloaded.getStatus());
    }

    @Test
    void shouldFailWhenListingNotFound() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                9999L,
                                buyer.getId()
                        )
                )
        );

        assertTrue(
                ex.getCause().getMessage().contains("Listing not found")
        );
    }

    @Test
    void shouldFailWhenListingNotActive() {

        Asset asset = createAsset();
        AssetUnity unity = createUnity(asset, seller);
        AssetListing listing = createListing(unity, AssetListingStatus.SOLD);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                listing.getId(),
                                buyer.getId()
                        )
                )
        );

        assertTrue(
                ex.getCause().getMessage().contains("Listing not active")
        );
    }

    @Test
    void shouldFailWhenBuyerIsSeller() {

        Asset asset = createAsset();
        AssetUnity unity = createUnity(asset, seller);
        AssetListing listing = createListing(unity, AssetListingStatus.ACTIVE);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                listing.getId(),
                                seller.getId()
                        )
                )
        );

        assertTrue(
                ex.getCause().getMessage().contains("Buyer cannot be seller")
        );
    }

    @Test
    void shouldRollbackWhenTransferFails() {

        Asset asset = createAsset();
        AssetUnity unity = createUnity(asset, seller);
        AssetListing listing = createListing(unity, AssetListingStatus.ACTIVE);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                listing.getId(),
                                -1L
                        )
                )
        );

        assertNotNull(ex.getMessage());

        AssetListing reloaded =
                assetListingService.selectById(listing.getId());

        assertEquals(
                AssetListingStatus.ACTIVE,
                reloaded.getStatus()
        );
    }
}
