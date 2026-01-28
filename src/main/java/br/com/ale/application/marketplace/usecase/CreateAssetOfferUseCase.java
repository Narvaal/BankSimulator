package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.service.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.AuthService;

public class CreateAssetOfferUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetUnityService assetUnityService;
    private final AuthService authService;

    public CreateAssetOfferUseCase(
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

    public AssetListing execute(CreateAssetOfferCommand command) {

        TokenClaims claims = authService.validateToken(command.token());

        AssetUnity assetUnity = assetUnityService.selectById(command.assetUnityId());

        long ownerClientId =
                accountService.getAccountById(assetUnity.getOwnerAccountId())
                        .getClientId();

        if (ownerClientId != claims.clientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this asset"
            );
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
