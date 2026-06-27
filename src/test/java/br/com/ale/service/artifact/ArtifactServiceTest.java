package br.com.ale.service.artifact;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactServiceTest {

    private ArtifactService assetService;
    private TestConnectionProvider provider;

    private static final String VALID_TEXT = "legendary blue dragon";
    private static final int VALID_TOTAL_SUPPLY = 100;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        assetService = new ArtifactService(provider);
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM artifact_price_history");
            stmt.execute("DELETE FROM artifact_transfer");
            stmt.execute("DELETE FROM artifact_listing");
            stmt.execute("DELETE FROM artifact_unit");
            stmt.execute("DELETE FROM artifact");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateAsset() {

        Artifact artifact = assetService.createAsset(
                new CreateArtifactRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        assertNotNull(artifact);
        assertTrue(artifact.getId() > 0);
        assertEquals(VALID_TEXT, artifact.getText());
        assertEquals(VALID_TOTAL_SUPPLY, artifact.getTotalSupply());
        assertNotNull(artifact.getCreatedAt());
    }

    @Test
    void shouldPersistAssetAndRetrieveById() {

        Artifact created = assetService.createAsset(
                new CreateArtifactRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        Artifact fetched = assetService.selectById(created.getId());

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
                exception.getCause().getMessage().contains("Artifact not found"),
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldFailWhenCreatingAssetWithBlankText() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assetService.createAsset(
                        new CreateArtifactRequest(
                                "   ",
                                VALID_TOTAL_SUPPLY
                        )
                )
        );

        assertNotNull(exception.getCause());
        assertTrue(
                exception.getCause().getMessage().contains("Artifact text cannot be blank"),
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldFailWhenCreatingAssetWithInvalidSupply() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> assetService.createAsset(
                        new CreateArtifactRequest(
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

        Artifact artifact = assetService.createAsset(
                new CreateArtifactRequest(
                        VALID_TEXT,
                        VALID_TOTAL_SUPPLY
                )
        );

        Instant now = Instant.now();

        assertTrue(
                artifact.getCreatedAt().isBefore(now) || artifact.getCreatedAt().equals(now),
                "createdAt should be set by database timestamp"
        );
    }
}
