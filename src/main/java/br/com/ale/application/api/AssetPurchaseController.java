package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.PurchaseAssetCommand;
import br.com.ale.application.marketplace.usecase.PurchaseAssetUseCase;
import br.com.ale.domain.asset.AssetPurchase;
import br.com.ale.dto.PurchaseAssetApiRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-listings")
public class AssetPurchaseController {

    private final PurchaseAssetUseCase purchaseAssetUseCase;
    private final AuthCookieService authCookieService;

    public AssetPurchaseController(PurchaseAssetUseCase purchaseAssetUseCase, AuthCookieService authCookieService) {
        this.purchaseAssetUseCase = purchaseAssetUseCase;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/{id}/purchase")
    public AssetPurchase purchase(
            @PathVariable("id") long listingId,
            HttpServletRequest httpRequest
    ) {
        String token = authCookieService.extractToken(httpRequest);
        PurchaseAssetCommand command = new PurchaseAssetCommand(listingId, token);
        return purchaseAssetUseCase.execute(command);
    }
}
