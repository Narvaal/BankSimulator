package br.com.ale.service.asset;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetListingServiceTest {

    private TestConnectionProvider provider;

    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetWebhookNotifier webhookNotifier;
    private ClientService clientService;
    private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;

    private long sellerAccountId;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);
        inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
        accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);
        clientService = new ClientService(provider);
        cleanDatabase();
        sellerAccountId = createAccount();
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

    private long createAccount() {
        Client client = clientService.createClient(
                new CreateClientRequest("John", "John@mail.com", "123", Provider.LOCAL,
                        null, false, null)
        );

        Account account = accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        "999999999",
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );

        return account.getId();
    }

    private Asset createAsset() {
        return assetService.createAsset(
                new CreateAssetRequest("Cool Asset", 10)
        );
    }

    private AssetUnity createAssetUnity(long assetId) {
        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(assetId, sellerAccountId)
        );
    }

    @Test
    void shouldCreateAssetListing() {

        Asset asset = createAsset();
        AssetUnity unity = createAssetUnity(asset.getId());

        AssetListing listing = assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity.getId(),
                        sellerAccountId,
                        new BigDecimal("100.00"),
                        AssetListingStatus.ACTIVE
                )
        );

        assertNotNull(listing);
        assertTrue(listing.getId() > 0);
        assertEquals(unity.getId(), listing.getAssetUnityId());
        assertEquals(sellerAccountId, listing.getSellerAccountId());
        assertEquals(new BigDecimal("100.00"), listing.getPrice());
        assertEquals(AssetListingStatus.ACTIVE, listing.getStatus());
        assertNotNull(listing.getCreatedAt());
    }

    @Test
    void shouldSelectAssetListingById() {

        Asset asset = createAsset();
        AssetUnity unity = createAssetUnity(asset.getId());

        AssetListing created = assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity.getId(),
                        sellerAccountId,
                        new BigDecimal("50.00"),
                        AssetListingStatus.ACTIVE
                )
        );

        AssetListing found =
                assetListingService.selectById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getAssetUnityId(), found.getAssetUnityId());
        assertEquals(created.getSellerAccountId(), found.getSellerAccountId());
    }

    @Test
    void shouldFailWhenAssetListingNotFound() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetListingService.selectById(9999L)
        );

        assertTrue(
                ex.getMessage().contains("Service error while selecting asset listing")
        );
        assertNotNull(ex.getCause());
        assertTrue(
                ex.getCause().getMessage().contains("Asset listing not found")
        );
    }

    @Test
    void shouldSelectAssetListingsByStatus() {

        Asset asset = createAsset();

        AssetUnity unity1 = createAssetUnity(asset.getId());
        AssetUnity unity2 = createAssetUnity(asset.getId());

        assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity1.getId(),
                        sellerAccountId,
                        new BigDecimal("10.00"),
                        AssetListingStatus.ACTIVE
                )
        );

        assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity2.getId(),
                        sellerAccountId,
                        new BigDecimal("20.00"),
                        AssetListingStatus.ACTIVE
                )
        );

        List<AssetListing> listings =
                assetListingService.selectByStatus(AssetListingStatus.ACTIVE);

        assertEquals(2, listings.size());
        assertTrue(
                listings.stream().allMatch(l -> l.getStatus() == AssetListingStatus.ACTIVE)
        );
    }

    @Test
    void shouldRollbackWhenInsertFails() {

        assertThrows(
                RuntimeException.class,
                () -> assetListingService.createAssetListing(
                        new CreateAssetListingRequest(
                                9999L,
                                sellerAccountId,
                                new BigDecimal("100.00"),
                                AssetListingStatus.ACTIVE
                        )
                )
        );

        List<AssetListing> listings =
                assetListingService.selectByStatus(AssetListingStatus.ACTIVE);

        assertTrue(listings.isEmpty());
    }
}
