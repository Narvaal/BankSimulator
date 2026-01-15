package br.com.ale.service.asset;

import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class AssetUnityServiceTest {

    private AssetUnityService assetUnityService;
    private TestConnectionProvider provider;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        assetUnityService = new AssetUnityService(provider);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Connection conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset_unit");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateAssetUnity() {

        CreateAssetUnityRequest request =
                new CreateAssetUnityRequest(
                        1L,
                        10L
                );

        AssetUnity assetUnity =
                assetUnityService.createAssetUnity(request);

        assertNotNull(assetUnity);
        assertTrue(assetUnity.getId() > 0);
        assertEquals(1L, assetUnity.getAssetId());
        assertEquals(10L, assetUnity.getOwnerAccountId());
        assertNotNull(assetUnity.getCreatedAt());
    }

    @Test
    void shouldSelectAssetUnityById() {

        CreateAssetUnityRequest request =
                new CreateAssetUnityRequest(
                        2L,
                        20L
                );

        AssetUnity created =
                assetUnityService.createAssetUnity(request);

        AssetUnity found =
                assetUnityService.selectById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(2L, found.getAssetId());
        assertEquals(20L, found.getOwnerAccountId());
        assertNotNull(found.getCreatedAt());
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

        assertNotNull(ex.getCause());
        assertTrue(
                ex.getCause().getMessage().contains("AssetUnity not found"),
                ex.getCause().getMessage()
        );
    }
}
