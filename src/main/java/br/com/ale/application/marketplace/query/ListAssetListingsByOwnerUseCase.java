package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.service.asset.AssetListingService;

import java.util.List;

public class ListAssetListingsByOwnerUseCase {

    private final AssetListingService assetListingService;

    public ListAssetListingsByOwnerUseCase(AssetListingService assetListingService) {
        this.assetListingService = assetListingService;
    }

    public List<AssetListing> execute(long ownerAccountId) {
        return assetListingService.selectByOwnerAccount(
                ownerAccountId,
                AssetListingStatus.ACTIVE
        );
    }
}
