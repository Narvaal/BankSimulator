package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.domain.account.*;
import br.com.ale.domain.client.*;
import br.com.ale.domain.asset.*;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseAssetUseCaseTest {

    private TestConnectionProvider provider;
    private InMemoryPrivateKeyStorage keyStorage;

    private ClientService clientService;
    private AccountService accountService;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;

    private AssetPurchaseService assetPurchaseService;
    private AssetPriceHistoryService assetPriceHistoryService;

    private PurchaseAssetUseCase useCase;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        keyStorage = new InMemoryPrivateKeyStorage();

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, keyStorage);

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider);
        assetListingService = new AssetListingService(provider);

        assetPurchaseService = new AssetPurchaseService(provider);
        assetPriceHistoryService = new AssetPriceHistoryService(provider);

        useCase =
                new PurchaseAssetUseCase(
                        accountService,
                        assetListingService,
                        assetPurchaseService,
                        assetPriceHistoryService
                );

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
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldPurchaseAssetSuccessfully() {

        Account seller = createAccount();
        Account buyer = createAccount();

        AssetListing listing = createListing(seller, new BigDecimal("100.00"));

        accountService.credit(
                buyer.getAccountNumber(),
                new BigDecimal("100.00")
        );

        PurchaseAssetCommand command =
                new PurchaseAssetCommand(
                        listing.getId(),
                        buyer.getId()
                );

        AssetPurchase purchase =
                assertDoesNotThrow(() ->
                        useCase.execute(command)
                );

        assertNotNull(purchase);
        assertEquals(listing.getId(), purchase.getListingId());
        assertEquals(buyer.getId(), purchase.getBuyerAccountId());
        assertEquals(seller.getId(), purchase.getSellerAccountId());
        assertEquals(new BigDecimal("100.00"), purchase.getPrice());
    }

    @Test
    void shouldFailWhenBuyerIsSeller() {

        Account seller = createAccount();
        AssetListing listing = createListing(seller, new BigDecimal("50.00"));

        PurchaseAssetCommand command =
                new PurchaseAssetCommand(
                        listing.getId(),
                        seller.getId()
                );

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> useCase.execute(command)
                );

        assertTrue(ex.getMessage().contains("Buyer cannot be seller"));
    }

    @Test
    void shouldFailWhenListingIsNotActive() {

        Account seller = createAccount();
        Account buyer = createAccount();

        AssetListing listing = createListing(seller, new BigDecimal("80.00"));

        assetListingService.updateStatus(
                listing.getId(),
                AssetListingStatus.SOLD
        );

        PurchaseAssetCommand command =
                new PurchaseAssetCommand(
                        listing.getId(),
                        buyer.getId()
                );

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> useCase.execute(command)
                );

        assertTrue(ex.getMessage().contains("Listing not active"));
    }

    private Account createAccount() {

        Client client =
                clientService.createClient(
                        new CreateClientRequest(
                                "Client " + System.nanoTime(),
                                String.valueOf(System.nanoTime())
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

    private AssetListing createListing(Account seller, BigDecimal price) {

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

        return assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity.getId(),
                        seller.getId(),
                        price,
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
