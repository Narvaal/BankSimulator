package br.com.ale.application.marketplace.usecase;

import br.com.ale.dto.AssetBundleResponse;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.service.asset.AssetBundleService;
import java.util.List;

public class CreateAssetBundleUseCase {

    private final AssetBundleService assetBundleService;

    public CreateAssetBundleUseCase(AssetBundleService assetBundleService) {
        this.assetBundleService = assetBundleService;
    }

    public AssetBundleResponse execute(
            List<CreateAssetRequest> assetRequests,
            String identifier
    ) {
        return assetBundleService.createBundle(assetRequests, identifier);
    }
}
