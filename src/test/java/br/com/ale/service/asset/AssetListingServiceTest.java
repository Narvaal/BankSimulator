package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetListingServiceTest {

    private TestConnectionProvider provider;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;

    private long sellerAccountId;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider);
        assetListingService = new AssetListingService(provider);

        cleanDatabase();
        sellerAccountId = createAccount();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset_listing");
            stmt.execute("DELETE FROM asset_transfer");
            stmt.execute("DELETE FROM asset_unit");
            stmt.execute("DELETE FROM asset");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long createAccount() {
        try (var conn = provider.getConnection()) {

            long clientId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO client (name, document) VALUES (?, ?)",
                    new String[]{"id"}
            )) {
                stmt.setString(1, "Test Client");
                stmt.setString(2, "123456789");
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    clientId = rs.getLong(1);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
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
                stmt.setString(2, "ACC-001");
                stmt.setString(3, "WALLET");
                stmt.setString(4, "ACTIVE");
                stmt.setBigDecimal(5, new BigDecimal("1000.00"));
                stmt.setString(6, "test-public-key");
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
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
