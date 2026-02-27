package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.asset.AssetUnityStatus;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.JwtService;

public class CreateAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final AssetUnityService assetUnityService;
    private final JwtService jwtService;

    public CreateAssetOfferUseCase(
            AssetListingService assetListingService,
            AssetUnityService assetUnityService,
            JwtService jwtService
    ) {
        this.assetListingService = assetListingService;
        this.assetUnityService = assetUnityService;
        this.jwtService = jwtService;
    }

    public AssetListing execute(CreateAssetOfferCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long authenticatedAccountId = jwtService.extractClientId(command.token());

        boolean locked = assetUnityService.tryUpdateToMarket(
                command.assetUnityId()
        );

        if (!locked) {
            throw new UnauthorizedOperationException(
                    "Asset not owned or not available"
            );
        }

        return assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        command.assetUnityId(),
                        authenticatedAccountId,
                        command.price(),
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
