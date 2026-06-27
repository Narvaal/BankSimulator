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
    void shouldGenerateValidAssetText() {
        Artifact artifact = assetGenerationService.generateWeeklyAssets().get(0);

        assertNotNull(artifact.getText());
        assertFalse(artifact.getText().isBlank());
        assertTrue(artifact.getText().contains(" "));
    }

    @Test
    void shouldHaveNonEmptyTextForAllGeneratedAssets() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        for (Artifact artifact : assets) {
            assertNotNull(artifact.getText());
            assertFalse(artifact.getText().isBlank());
        }
    }

    @Test
    void shouldNotGenerateDuplicateAssetsInSameWeek() {
        List<Artifact> assets = assetGenerationService.generateWeeklyAssets();

        Set<String> uniqueTexts = new HashSet<>();

        for (Artifact artifact : assets) {
            uniqueTexts.add(artifact.getText());
        }

        assertEquals(
                assets.size(),
                uniqueTexts.size(),
                "Assets should be unique within the same week"
        );
    }

    @Test
    void shouldGenerateDifferentAssetsOnDifferentRuns() {
        List<Artifact> firstRun = assetGenerationService.generateWeeklyAssets();
        List<Artifact> secondRun = assetGenerationService.generateWeeklyAssets();

        assertNotEquals(
                extractTexts(firstRun),
                extractTexts(secondRun),
                "Two generations should not be identical"
        );
    }

    private List<String> extractTexts(List<Artifact> assets) {
        return assets.stream()
                .map(Artifact::getText)
                .toList();
    }
}
