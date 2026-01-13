package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssetGenerationServiceTest {

    private final AssetGenerationService assetGenerationService =
            new AssetGenerationService();

    @Test
    void shouldGenerateWeeklyAssets() {
        List<Asset> assets = assetGenerationService.generateWeeklyAssets();

        assertNotNull(assets);
        assertFalse(assets.isEmpty());
    }

    @Test
    void shouldGenerateCorrectAmountOfAssets() {
        List<Asset> assets = assetGenerationService.generateWeeklyAssets();

        assertEquals(
                AssetGenerationService.NUMBER_OF_ASSETS_PER_WEEK,
                assets.size()
        );
    }

    @Test
    void shouldGenerateValidAssetText() {
        Asset asset = assetGenerationService.generateWeeklyAssets().get(0);

        assertNotNull(asset.getText());
        assertFalse(asset.getText().isBlank());
        assertTrue(asset.getText().contains(" "));
    }

    @Test
    void allAssetsShouldHaveNonEmptyText() {
        List<Asset> assets = assetGenerationService.generateWeeklyAssets();

        for (Asset asset : assets) {
            assertNotNull(asset.getText());
            assertFalse(asset.getText().isBlank());
        }
    }

    @Test
    void shouldNotGenerateDuplicateAssetsInSameWeek() {
        List<Asset> assets = assetGenerationService.generateWeeklyAssets();

        Set<String> uniqueTexts = new HashSet<>();

        for (Asset asset : assets) {
            uniqueTexts.add(asset.getText());
        }

        assertEquals(
                assets.size(),
                uniqueTexts.size(),
                "Assets should be unique within the same week"
        );
    }

    @Test
    void shouldGenerateDifferentAssetsOnDifferentRuns() {
        List<Asset> firstRun = assetGenerationService.generateWeeklyAssets();
        List<Asset> secondRun = assetGenerationService.generateWeeklyAssets();

        assertNotEquals(
                extractTexts(firstRun),
                extractTexts(secondRun),
                "Two generations should not be identical"
        );
    }

    private List<String> extractTexts(List<Asset> assets) {
        return assets.stream()
                .map(Asset::getText)
                .toList();
    }
}
