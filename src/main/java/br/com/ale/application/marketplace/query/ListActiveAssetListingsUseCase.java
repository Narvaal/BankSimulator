package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.service.asset.AssetListingService;

import java.util.List;

public class ListActiveAssetListingsUseCase {

    private final AssetListingService assetListingService;

    public ListActiveAssetListingsUseCase(AssetListingService assetListingService) {
        this.assetListingService = assetListingService;
    }

    public List<AssetListing> execute() {
        return assetListingService.selectByStatus(AssetListingStatus.ACTIVE);
    }
}
