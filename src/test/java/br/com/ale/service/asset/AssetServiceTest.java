package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AssetServiceTest {

    private AssetService assetService;
    private TestConnectionProvider provider;

    private static final String VALID_TEXT = "legendary blue dragon";
    private static final int VALID_TOTAL_SUPPLY = 100;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        assetService = new AssetService(provider);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateAsset() {

        Asset asset = assetService.createAsset(
                new CreateAssetRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        assertNotNull(asset);
        assertTrue(asset.getId() > 0);
        assertEquals(VALID_TEXT, asset.getText());
        assertEquals(VALID_TOTAL_SUPPLY, asset.getTotalSupply());
        assertNotNull(asset.getCreatedAt());
    }

    @Test
    void shouldPersistAssetAndRetrieveById() {

        Asset created = assetService.createAsset(
                new CreateAssetRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        Asset fetched = assetService.selectById(created.getId());

        assertEquals(created.getId(), fetched.getId());
        assertEquals(created.getText(), fetched.getText());
        assertEquals(created.getTotalSupply(), fetched.getTotalSupply());
        assertEquals(created.getCreatedAt(), fetched.getCreatedAt());
    }

    @Test
    void shouldFailWhenAssetNotFound() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assetService.selectById(9999L)
        );

        assertNotNull(exception.getCause());

        assertTrue(
                exception.getCause().getMessage().contains("Asset not found"),
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldFailWhenCreatingAssetWithBlankText() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assetService.createAsset(
                        new CreateAssetRequest(
                                "   ",
                                VALID_TOTAL_SUPPLY
                        )
                )
        );

        assertNotNull(exception.getCause());
        assertTrue(
                exception.getCause().getMessage().contains("Asset text cannot be blank"),
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldFailWhenCreatingAssetWithInvalidSupply() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assetService.createAsset(
                        new CreateAssetRequest(
                                VALID_TEXT,
                                0
                        )
                )
        );

        String message =
                exception.getCause() != null
                        ? exception.getCause().getMessage()
                        : exception.getMessage();

        assertTrue(
                message.contains("total supply"),
                message
        );
    }

    @Test
    void shouldSetCreatedAtFromDatabase() {

        Asset asset = assetService.createAsset(
                new CreateAssetRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        Instant now = Instant.now();

        assertTrue(
                asset.getCreatedAt().isBefore(now) || asset.getCreatedAt().equals(now),
                "createdAt should be set by database timestamp"
        );
    }
}
