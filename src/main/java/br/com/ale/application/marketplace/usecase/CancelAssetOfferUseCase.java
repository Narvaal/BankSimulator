package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.AuthService;

public class CancelAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final AuthService authService;

    public CancelAssetOfferUseCase(
            AssetListingService assetListingService,
            AuthService authService
    ) {
        this.assetListingService = assetListingService;
        this.authService = authService;
    }

    public void execute(CancelAssetCommand command) {

        TokenClaims claims = authService.validateToken(command.token());

        assetListingService.cancelListing(
                command.assetListingId(),
                claims.clientId()
        );
    }
}
