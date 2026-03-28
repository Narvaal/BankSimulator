package br.com.ale.application.marketplace.query;

import br.com.ale.dto.AssetSummaryResponse;
import br.com.ale.service.asset.AssetService;
import java.util.List;

public class ListAssetsUseCase {

    private final AssetService assetService;

    public ListAssetsUseCase(AssetService assetService) {
        this.assetService = assetService;
    }

    public List<AssetSummaryResponse> execute() {
        return assetService.listAssets();
    }
}
