package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AssetUnityServiceTest {

    private TestConnectionProvider provider;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetWebhookNotifier webhookNotifier;

    private long ownerAccountId;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);

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
        try (var conn = provider.getConnection()) {

            long clientId;
            try (var stmt = conn.prepareStatement(
                    "INSERT INTO client (name, email) VALUES (?, ?)",
                    new String[]{"id"}
            )) {
                stmt.setString(1, "Client");
                stmt.setString(2, "DOC-1");
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
                stmt.setString(2, "ACC-1");
                stmt.setString(3, "WALLET");
                stmt.setString(4, "ACTIVE");
                stmt.setBigDecimal(5, new BigDecimal("1000.00"));
                stmt.setString(6, "pk-1");
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
