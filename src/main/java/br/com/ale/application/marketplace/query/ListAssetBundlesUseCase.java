package br.com.ale.application.marketplace.query;

import br.com.ale.dto.AssetBundleResponse;
import br.com.ale.service.asset.AssetBundleService;
import java.util.List;

public class ListAssetBundlesUseCase {

    private final AssetBundleService assetBundleService;

    public ListAssetBundlesUseCase(AssetBundleService assetBundleService) {
        this.assetBundleService = assetBundleService;
    }

    public List<AssetBundleResponse> execute() {
        return assetBundleService.listBundles();
    }
}
