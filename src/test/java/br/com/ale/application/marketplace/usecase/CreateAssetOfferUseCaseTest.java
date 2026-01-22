package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CreateAssetOfferUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;

    private CreateAssetOfferUseCase useCase;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider);
        assetListingService = new AssetListingService(provider);

        useCase = new CreateAssetOfferUseCase(
                assetListingService,
                assetUnityService
        );

        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

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
    void shouldCreateOfferSuccessfully() {

        Account owner = createAccount();
        AssetUnity unity = createAssetUnity(owner);

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        unity.getId(),
                        owner.getId(),
                        new BigDecimal("150.00")
                );

        AssetListing listing =
                assertDoesNotThrow(() -> useCase.execute(command));

        assertNotNull(listing);
        assertEquals(unity.getId(), listing.getAssetUnityId());
        assertEquals(owner.getId(), listing.getSellerAccountId());
        assertEquals(new BigDecimal("150.00"), listing.getPrice());
        assertEquals(AssetListingStatus.ACTIVE, listing.getStatus());
    }

    @Test
    void shouldFailWhenAccountIsNotOwner() {

        Account owner = createAccount();
        Account otherAccount = createAccount();

        AssetUnity unity = createAssetUnity(owner);

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        unity.getId(),
                        otherAccount.getId(),
                        new BigDecimal("200.00")
                );

        assertThrows(
                RuntimeException.class,
                () -> useCase.execute(command)
        );
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
}
