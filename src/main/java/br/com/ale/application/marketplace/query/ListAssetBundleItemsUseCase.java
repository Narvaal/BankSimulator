package br.com.ale.application.marketplace.query;

import br.com.ale.dto.AssetBundleItemResponse;
import br.com.ale.service.asset.AssetBundleService;
import java.util.List;

public class ListAssetBundleItemsUseCase {

    private final AssetBundleService assetBundleService;

    public ListAssetBundleItemsUseCase(AssetBundleService assetBundleService) {
        this.assetBundleService = assetBundleService;
    }

    public List<AssetBundleItemResponse> execute(long bundleId) {
        return assetBundleService.listBundleItems(bundleId);
    }
}
