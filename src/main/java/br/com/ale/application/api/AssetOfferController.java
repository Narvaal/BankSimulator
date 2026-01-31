package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CreateAssetOfferCommand;
import br.com.ale.application.marketplace.usecase.CreateAssetOfferUseCase;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.dto.CreateAssetOfferApiRequest;
import java.math.BigDecimal;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-offers")
public class AssetOfferController {

    private final CreateAssetOfferUseCase createAssetOfferUseCase;

    public AssetOfferController(CreateAssetOfferUseCase createAssetOfferUseCase) {
        this.createAssetOfferUseCase = createAssetOfferUseCase;
    }

    @PostMapping
    public AssetListing create(
            @RequestBody CreateAssetOfferApiRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractToken(authorization, request.token());
        BigDecimal price = request.price();
        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        request.accountId(),
                        request.assetUnityId(),
                        price,
                        token
                );
        return createAssetOfferUseCase.execute(command);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
