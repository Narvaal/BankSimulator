package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.CreateAssetRequest;
import java.util.ArrayList;
import java.util.List;

public class AssetGenerationManager {

    private final AssetGenerationService assetGenerationService;
    private final AssetService assetService;

    public AssetGenerationManager(
            AssetGenerationService assetGenerationService,
            AssetService assetService
    ) {
        this.assetGenerationService = assetGenerationService;
        this.assetService = assetService;
    }

    public List<Asset> generateWeeklyAssets() {
        List<Asset> generated = assetGenerationService.generateWeeklyAssets();
        List<Asset> persisted = new ArrayList<>(generated.size());
        for (Asset asset : generated) {
            persisted.add(
                    assetService.createAsset(
                            new CreateAssetRequest(
                                    asset.getText(),
                                    asset.getTotalSupply()
                            )
                    )
            );
        }
        return persisted;
    }
}
