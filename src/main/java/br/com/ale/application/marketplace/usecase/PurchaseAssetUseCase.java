package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;

public class PurchaseAssetUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetPurchaseService assetPurchaseService;
    private final AssetPriceHistoryService assetPriceHistoryService;
    private final AuthService authService;

    public PurchaseAssetUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetPurchaseService assetPurchaseService,
            AssetPriceHistoryService assetPriceHistoryService,
            AuthService authService
    ) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.assetPurchaseService = assetPurchaseService;
        this.assetPriceHistoryService = assetPriceHistoryService;
        this.authService = authService;
    }

    public AssetPurchase execute(PurchaseAssetCommand command) {

        TokenClaims authenticatedAccount =
                authService.validateToken(command.token());

        AssetListing listing =
                assetListingService.selectById(command.listingId());

        if (listing.getStatus() != AssetListingStatus.ACTIVE) {
            throw new InvalidAssetListingStateException(listing.getId());
        }

        Account buyerAccount =
                accountService.getAccountById(command.buyerAccountId());

        if (authenticatedAccount.clientId() != buyerAccount.getClientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated account is not the buyer"
            );
        }

        if (listing.getSellerAccountId() == command.buyerAccountId()) {
            throw new UnauthorizedOperationException(
                    "Seller cannot buy own asset"
            );
        }

        accountService.transfer(
                command.buyerAccountId(),
                listing.getSellerAccountId(),
                listing.getPrice()
        );

        AssetPurchase purchase =
                assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                listing.getId(),
                                command.buyerAccountId()
                        )
                );

        assetPriceHistoryService.registerPriceChange(
                listing.getId(),
                listing.getPrice(),
                command.buyerAccountId(),
                ReasonType.SOLD
        );

        return purchase;
    }
}
