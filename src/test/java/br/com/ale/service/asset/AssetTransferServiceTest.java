package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetTransfer;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.dto.CreateAssetTransferRequest;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssetTransferServiceTest {

    private TestConnectionProvider provider;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetTransferService assetTransferService;
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

    private long createAccount(String suffix) {
        try (var conn = provider.getConnection()) {

            long clientId;
            try (var stmt = conn.prepareStatement(
                    "INSERT INTO client (name, email) VALUES (?, ?)",
                    new String[]{"id"}
            )) {
                stmt.setString(1, "Client " + suffix);
                stmt.setString(2, "DOC-" + suffix);
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    clientId = rs.getLong(1);
                }
            }

            try (var stmt = conn.prepareStatement(
                    """
                            INSERT INTO account (
                                client_id,
                                account_number,
                                account_type,
                                status,
                                balance,
                                public_key
                            )
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            )) {
                stmt.setLong(1, clientId);
                stmt.setString(2, "ACC-" + suffix);
                stmt.setString(3, "WALLET");
                stmt.setString(4, "ACTIVE");
                stmt.setBigDecimal(5, new BigDecimal("1000.00"));
                stmt.setString(6, "pk-" + suffix);
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
