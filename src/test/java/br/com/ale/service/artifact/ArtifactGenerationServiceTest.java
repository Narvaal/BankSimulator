package br.com.ale.service.artifact;

import br.com.ale.domain.artifact.Artifact;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactGenerationServiceTest {

    private final ArtifactGenerationService assetGenerationService =
            new ArtifactGenerationService();

    @Test
    void shouldGenerateWeeklyAssets() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        assertNotNull(assets);
        assertFalse(assets.isEmpty());
    }

    @Test
    void shouldGenerateCorrectAmountOfAssets() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        assertEquals(
                ArtifactGenerationService.NUMBER_OF_ASSETS_PER_WEEK,
                assets.size()
        );
    }

    @Test
    void shouldGenerateValidArtifactName() {
        Artifact artifact = assetGenerationService.generateWeeklyAssets().get(0);

        assertNotNull(artifact.getName());
        assertFalse(artifact.getName().isBlank());
        assertTrue(artifact.getName().contains(" "));
    }

    @Test
    void shouldHaveNonEmptyNameForAllGeneratedAssets() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        for (Artifact artifact : assets) {
            assertNotNull(artifact.getName());
            assertFalse(artifact.getName().isBlank());
        }
    }

    @Test
    void shouldNotGenerateDuplicateAssetsInSameWeek() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        Set<String> uniqueNames = new HashSet<>();

        for (Artifact artifact : assets) {
            uniqueNames.add(artifact.getName());
        }

        assertEquals(
                assets.size(),
                uniqueNames.size(),
                "Assets should be unique within the same week"
        );
    }

    @Test
    void shouldGenerateDifferentAssetsOnDifferentRuns() {
        List<Artifact> firstRun = assetGenerationService.generateWeeklyAssets();
        List<Artifact> secondRun = assetGenerationService.generateWeeklyAssets();

        assertNotEquals(
                extractNames(firstRun),
                extractNames(secondRun),
                "Two generations should not be identical"
        );
    }

    private List<String> extractNames(List<Artifact> assets) {
        return assets.stream()
                .map(Artifact::getName)
                .toList();
    }
}
