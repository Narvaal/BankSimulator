package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;

public class CreateAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final AssetUnityService assetUnityService;

    public CreateAssetOfferUseCase(
            AssetListingService assetListingService,
            AssetUnityService assetUnityService
    ) {
        this.assetListingService = assetListingService;
        this.assetUnityService = assetUnityService;
    }

    public AssetListing execute(CreateAssetOfferCommand command) {

        AssetUnity assetUnity = assetUnityService.selectById(command.assetUnityId());

        if (!assetUnity.getOwnerAccountId().equals(command.accountId())) {
            throw new UnauthorizedOperationException("Account is not the owner of this asset");
        }

        return assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        assetUnity.getId(),
                        assetUnity.getOwnerAccountId(),
                        command.price(),
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
