package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.service.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.AuthService;

public class CancelAssetOfferUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetUnityService assetUnityService;
    private final AuthService authService;

    public CancelAssetOfferUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetUnityService assetUnityService,
            AuthService authService
    ) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.assetUnityService = assetUnityService;
        this.authService = authService;
    }

    public void execute(CancelAssetCommand command) {

        TokenClaims claims =
                authService.validateToken(command.token());

        AssetListing listing =
                assetListingService.selectById(command.assetListingId());

        if (listing.getStatus() != AssetListingStatus.ACTIVE) {
            throw new InvalidAssetListingStateException(command.assetListingId());
        }

        AssetUnity unity =
                assetUnityService.selectById(listing.getAssetUnityId());

        long ownerClientId =
                accountService.getAccountById(unity.getOwnerAccountId())
                        .getClientId();

        if (ownerClientId != claims.clientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this asset"
            );
        }

        assetListingService.updateStatus(
                listing.getId(),
                AssetListingStatus.CANCELED
        );
    }
}
