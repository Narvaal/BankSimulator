package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.application.marketplace.usecase.CreateAssetOfferUseCase;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.dto.CreateAssetOfferApiRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset-offers")
public class AssetOfferController {

    private final CreateAssetOfferUseCase createAssetOfferUseCase;
    private final AuthCookieService authCookieService;

    public AssetOfferController(CreateAssetOfferUseCase createAssetOfferUseCase,
                                AuthCookieService authCookieService) {
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
                        body.accountId(),
                        body.assetUnityId(),
                        body.price(),
                        token
                );

        return createAssetOfferUseCase.execute(command);
    }
}
