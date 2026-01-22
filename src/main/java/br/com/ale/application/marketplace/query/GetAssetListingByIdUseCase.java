package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetListing;
import br.com.ale.service.asset.AssetListingService;

public class GetAssetListingByIdUseCase {

    private final AssetListingService assetListingService;

    public GetAssetListingByIdUseCase(AssetListingService assetListingService) {
        this.assetListingService = assetListingService;
    }

    public AssetListing execute(long listingId) {
        return assetListingService.selectById(listingId);
    }
}
