package br.com.ale.application.marketplace;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.asset.*;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.service.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetTransferService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;

public class PurchaseAssetUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetPurchaseService assetPurchaseService;
    private final AssetTransferService assetTransferService;
    private final AssetPriceHistoryService assetPriceHistoryService;

    public PurchaseAssetUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetPurchaseService assetPurchaseService,
            AssetTransferService assetTransferService,
            AssetPriceHistoryService assetPriceHistoryService
    ) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.assetPurchaseService = assetPurchaseService;
        this.assetTransferService = assetTransferService;
        this.assetPriceHistoryService = assetPriceHistoryService;
    }

    public AssetPurchase execute(PurchaseAssetCommand command) {

        AssetListing listing =
                assetListingService.selectById(command.listingId());

        if (listing.getStatus() != AssetListingStatus.ACTIVE) {
            throw new RuntimeException("Listing not active");
        }

        if (listing.getSellerAccountId() == command.buyerAccountId()) {
            throw new RuntimeException("Buyer cannot be seller");
        }

        Account account = accountService.getAccountById(command.buyerAccountId());

        accountService.debit(
                account.getAccountNumber(),
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
