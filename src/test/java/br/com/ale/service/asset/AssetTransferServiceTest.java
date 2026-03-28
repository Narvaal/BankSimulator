package br.com.ale.service.asset;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetTransfer;
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

import static org.junit.jupiter.api.Assertions.*;

class AssetTransferServiceTest {

    private TestConnectionProvider provider;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetTransferService assetTransferService;
    private AccountService accountService;
    private ClientService clientService;
    private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;
    private AssetWebhookNotifier webhookNotifier;

    private long fromAccountId;
    private long toAccountId;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetTransferService = new AssetTransferService(provider, webhookNotifier);
        inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
        accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
        clientService = new ClientService(provider);

        cleanDatabase();
        createAccounts();
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

    private void createAccounts() {
        fromAccountId = createAccount("FROM");
        toAccountId = createAccount("TO");
    }

    private long createAccount(String email) {
        Client client = clientService.createClient(
                new CreateClientRequest("john", email, "123", Provider.LOCAL,
                        null, false, null)
        );

        Account account = accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        "999999999" + email,
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );

        return account.getId();
    }

    private AssetUnity createAssetUnity() {
        Asset asset = assetService.createAsset(
                new CreateAssetRequest("Asset", 1)
        );

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(asset.getId(), fromAccountId)
        );
    }

    @Test
    void shouldCreateAssetTransfer() {

        AssetUnity unity = createAssetUnity();

        AssetTransfer transfer =
                assetTransferService.createAsset(
                        new CreateAssetTransferRequest(
                                unity.getId(),
                                fromAccountId,
                                toAccountId
                        )
                );

        assertNotNull(transfer);
        assertTrue(transfer.getId() > 0);
        assertEquals(unity.getId(), transfer.getAssetUnityId());
        assertEquals(fromAccountId, transfer.getFromAccountId());
        assertEquals(toAccountId, transfer.getToAccountId());
        assertNotNull(transfer.getCreatedAt());
    }

    @Test
    void shouldSelectAssetTransferById() {

        AssetUnity unity = createAssetUnity();

        AssetTransfer created =
                assetTransferService.createAsset(
                        new CreateAssetTransferRequest(
                                unity.getId(),
                                fromAccountId,
                                toAccountId
                        )
                );

        AssetTransfer found =
                assetTransferService.selectById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getAssetUnityId(), found.getAssetUnityId());
        assertEquals(created.getFromAccountId(), found.getFromAccountId());
        assertEquals(created.getToAccountId(), found.getToAccountId());
    }

    @Test
    void shouldFailWhenTransferToSameAccount() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetTransferService.createAsset(
                        new CreateAssetTransferRequest(
                                1L,
                                fromAccountId,
                                fromAccountId
                        )
                )
        );

        assertTrue(
                ex.getMessage().contains("Not allowed asset transfer to the same account"),
                ex.getMessage()
        );
    }

    @Test
    void shouldFailWhenAssetTransferNotFound() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetTransferService.selectById(9999L)
        );

        assertTrue(
                ex.getMessage().contains("Service error while selecting asset transfer"),
                ex.getMessage()
        );
    }

    @Test
    void shouldRollbackWhenInsertFails() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetTransferService.createAsset(
                        new CreateAssetTransferRequest(
                                -1L,
                                fromAccountId,
                                toAccountId
                        )
                )
        );

        assertNotNull(ex.getMessage());
        assertTrue(
                ex.getMessage().contains("Service error while creating asset Transfer"),
                ex.getMessage()
        );
    }
}
