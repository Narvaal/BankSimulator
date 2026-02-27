package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.asset.*;
import br.com.ale.domain.exception.InvalidAssetListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;

public class PurchaseAssetUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final AssetPurchaseService assetPurchaseService;
    private final AssetPriceHistoryService assetPriceHistoryService;
    private final JwtService jwtService;

    public PurchaseAssetUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetPurchaseService assetPurchaseService,
            AssetPriceHistoryService assetPriceHistoryService,
            JwtService jwtService
    ) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.assetPurchaseService = assetPurchaseService;
        this.assetPriceHistoryService = assetPriceHistoryService;
        this.jwtService = jwtService;
    }

    public AssetPurchase execute(PurchaseAssetCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());

        Account authenticated = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedOperationException("Account not found"));

        AssetListing listing = assetListingService.selectById(command.listingId());

        if (listing.getSellerAccountId() == authenticated.getId()) {
            throw new UnauthorizedOperationException("Seller cannot buy own asset");
        }

        AssetPurchase purchase = assetPurchaseService.purchase(
                new CreateAssetPurchaseRequest(
                        listing.getId(),
                        authenticated.getId()
                )
        );

        assetPriceHistoryService.registerPriceChange(
                listing.getId(),
                listing.getPrice(),
                authenticated.getId(),
                ReasonType.SOLD
        );

        return purchase;
    }
}
