package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.JwtService;

public class CancelAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final JwtService jwtService;

    public CancelAssetOfferUseCase(
            AssetListingService assetListingService,
            JwtService jwtService
    ) {
        this.assetListingService = assetListingService;
        this.jwtService = jwtService;
    }

    public void execute(CancelAssetCommand command) {

        long clientId = jwtService.extractClientId(command.token());

        assetListingService.cancelListing(
                command.assetListingId(),
                clientId
        );
    }
}
