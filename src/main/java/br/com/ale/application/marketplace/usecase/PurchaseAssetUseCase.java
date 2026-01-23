package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.service.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;

public class PurchaseAssetUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetPurchaseService assetPurchaseService;
    private final AssetPriceHistoryService assetPriceHistoryService;

    public PurchaseAssetUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetPurchaseService assetPurchaseService,
            AssetPriceHistoryService assetPriceHistoryService
    ) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.assetPurchaseService = assetPurchaseService;
        this.assetPriceHistoryService = assetPriceHistoryService;
    }

    public AssetPurchase execute(PurchaseAssetCommand command) {

        AssetListing listing =
                assetListingService.selectById(command.listingId());

        if (listing.getStatus() != AssetListingStatus.ACTIVE) {
            throw new InvalidAssetListingStateException(command.listingId());
        }

        if (listing.getSellerAccountId() == command.buyerAccountId()) {
            throw new UnauthorizedOperationException("Buyer cannot be seller");
        }

        accountService.transfer(
                command.buyerAccountId(),
                listing.getSellerAccountId(),
                listing.getPrice()
        );

        AssetPurchase purchase =
                assetPurchaseService.purchase(
                        new CreateAssetPurchaseRequest(
                                command.listingId(),
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
