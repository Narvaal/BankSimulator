package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.CreateAssetRequest;
import java.util.ArrayList;
import java.util.List;

public class AssetGenerationManager {

    private final AssetGenerationService assetGenerationService;
    private final AssetBundleService assetBundleService;

    public AssetGenerationManager(
            AssetGenerationService assetGenerationService,
            AssetBundleService assetBundleService
    ) {
        this.assetGenerationService = assetGenerationService;
        this.assetBundleService = assetBundleService;
    }

    public List<Asset> generateWeeklyAssets() {
        List<Asset> generated = assetGenerationService.generateWeeklyAssets();
        return assetBundleService.createWeeklyBundle(generated);
    }
}
