package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;

public class CancelAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final AssetUnityService assetUnityService;

    public CancelAssetOfferUseCase(
            AssetListingService assetListingService,
            AssetUnityService assetUnityService
    ) {
        this.assetListingService = assetListingService;
        this.assetUnityService = assetUnityService;
    }

    public void execute(CancelAssetCommand command) {

        AssetListing listing =
                assetListingService.selectById(command.assetListingId());

        if (listing.getStatus() != AssetListingStatus.ACTIVE) {
            throw new RuntimeException("Only active listings can be canceled");
        }

        AssetUnity unity =
                assetUnityService.selectById(listing.getAssetUnityId());

        if (unity.getOwnerAccountId() != command.accountId()) {
            throw new RuntimeException("Account is not the owner of this asset");
        }

        assetListingService.updateStatus(
                listing.getId(),
                AssetListingStatus.CANCELED
        );
    }
}
