package br.com.ale.service.asset;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class AssetUnityServiceTest {

    private TestConnectionProvider provider;
    private ClientService clientService;
    private AssetService assetService;
    private AccountService accountService;
    private AssetUnityService assetUnityService;
    private AssetWebhookNotifier webhookNotifier;
    private InMemoryPrivateKeyStorage inMemoryPrivateKeyStorage;
    private long ownerAccountId;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);
        inMemoryPrivateKeyStorage = new InMemoryPrivateKeyStorage();
        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        accountService = new AccountService(provider, inMemoryPrivateKeyStorage);
        clientService = new ClientService(provider);

        cleanDatabase();
        ownerAccountId = createAccount();
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

    @Test
    void shouldCreateAssetUnity() {

        Asset asset = createAsset();

        AssetUnity unity =
                assetUnityService.createAssetUnity(
                        new CreateAssetUnityRequest(
                                asset.getId(),
                                ownerAccountId
                        )
                );

        assertNotNull(unity);
        assertTrue(unity.getId() > 0);
        assertEquals(asset.getId(), unity.getAssetId());
        assertEquals(ownerAccountId, unity.getOwnerAccountId());
        assertNotNull(unity.getCreatedAt());
    }

    @Test
    void shouldSelectAssetUnityById() {

        Asset asset = createAsset();

        AssetUnity created =
                assetUnityService.createAssetUnity(
                        new CreateAssetUnityRequest(
                                asset.getId(),
                                ownerAccountId
                        )
                );

        AssetUnity found =
                assetUnityService.selectById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getAssetId(), found.getAssetId());
        assertEquals(created.getOwnerAccountId(), found.getOwnerAccountId());
    }

    @Test
    void shouldFailWhenAssetUnityNotFound() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetUnityService.selectById(9999L)
        );

        assertTrue(
                ex.getMessage().contains("Service error while selecting assetUnity"),
                ex.getMessage()
        );
    }

    @Test
    void shouldRollbackWhenInsertFails() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> assetUnityService.createAssetUnity(
                        new CreateAssetUnityRequest(
                                -1L,
                                ownerAccountId
                        )
                )
        );

        assertNotNull(ex.getMessage());
        assertTrue(
                ex.getMessage().contains("Service error while creating assetUnity"),
                ex.getMessage()
        );
    }
}
