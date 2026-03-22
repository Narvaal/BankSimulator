package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.JwtService;

public class CreateAssetOfferUseCase {

    private final AssetListingService assetListingService;
    private final AccountService accountService;
    private final JwtService jwtService;

    public CreateAssetOfferUseCase(
            AssetListingService assetListingService,
            AccountService accountService,
            JwtService jwtService
    ) {
        this.assetListingService = assetListingService;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public AssetListing execute(CreateAssetOfferCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());

        Account account = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new InvalidCredentialsException("Client not found"));

        return assetListingService.createAssetOffer(
                new CreateAssetListingRequest(
                        command.assetUnityId(),
                        account.getId(),
                        command.price(),
                        AssetListingStatus.ACTIVE
                )
        );
    }
}
