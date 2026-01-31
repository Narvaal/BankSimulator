package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.Asset;
import br.com.ale.service.asset.AssetService;

public class GetAssetByIdUseCase {

    private final AssetService assetService;

    public GetAssetByIdUseCase(AssetService assetService) {
        this.assetService = assetService;
    }

    public Asset execute(long assetId) {
        return assetService.selectById(assetId);
    }
}
