package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.application.marketplace.usecase.CancelAssetOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateAssetOfferUseCase;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.dto.CancelAssetOfferApiRequest;
import br.com.ale.dto.CreateAssetOfferApiRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-offers")
public class AssetOfferController {

    private final CancelAssetOfferUseCase cancelAssetOfferUseCase;
    private final CreateAssetOfferUseCase createAssetOfferUseCase;
    private final AuthCookieService authCookieService;

    public AssetOfferController(CancelAssetOfferUseCase cancelAssetOfferUseCase,
                                CreateAssetOfferUseCase createAssetOfferUseCase,
                                AuthCookieService authCookieService) {
        this.cancelAssetOfferUseCase = cancelAssetOfferUseCase;
        this.createAssetOfferUseCase = createAssetOfferUseCase;
        this.authCookieService = authCookieService;
    }

    @PostMapping
    public AssetListing create(
            @RequestBody CreateAssetOfferApiRequest body,
            HttpServletRequest httpRequest
    ) {
        String token = authCookieService.extractToken(httpRequest);

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        body.assetUnityId(),
                        body.price(),
                        token
                );

        return createAssetOfferUseCase.execute(command);
    }

    @PostMapping("/cancel")
    public void cancel(
            @RequestBody CancelAssetOfferApiRequest body,
            HttpServletRequest httpRequest
    ) {
        String token = authCookieService.extractToken(httpRequest);

        cancelAssetOfferUseCase.execute(
                new CancelAssetCommand(
                        body.accountId(),
                        body.assetListingId(),
                        token

                )
        );
    }
}
