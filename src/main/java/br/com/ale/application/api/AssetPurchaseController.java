package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.application.marketplace.usecase.PurchaseAssetUseCase;
import br.com.ale.domain.asset.AssetPurchase;
import br.com.ale.dto.PurchaseAssetApiRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-listings")
public class AssetPurchaseController {

    private final PurchaseAssetUseCase purchaseAssetUseCase;

    public AssetPurchaseController(PurchaseAssetUseCase purchaseAssetUseCase) {
        this.purchaseAssetUseCase = purchaseAssetUseCase;
    }

    @PostMapping("/{id}/purchase")
    public AssetPurchase purchase(
            @PathVariable("id") long listingId,
            @RequestBody PurchaseAssetApiRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractToken(authorization, request.token());
        PurchaseAssetCommand command =
                new PurchaseAssetCommand(request.buyerAccountId(), listingId, token);
        return purchaseAssetUseCase.execute(command);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
