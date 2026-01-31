package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetAssetByIdUseCase;
import br.com.ale.application.marketplace.query.ListAssetsUseCase;
import br.com.ale.application.marketplace.query.ListAssetPriceHistoryByAssetIdUseCase;
import br.com.ale.application.marketplace.usecase.CreateAssetUnityForAccountUseCase;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.AssetSummaryResponse;
import br.com.ale.dto.CreateAssetUnityApiRequest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets")
public class AssetQueryController {

    private final GetAssetByIdUseCase getAssetByIdUseCase;
    private final ListAssetPriceHistoryByAssetIdUseCase listAssetPriceHistoryByAssetIdUseCase;
    private final ListAssetsUseCase listAssetsUseCase;
    private final CreateAssetUnityForAccountUseCase createAssetUnityForAccountUseCase;

    public AssetQueryController(
            GetAssetByIdUseCase getAssetByIdUseCase,
            ListAssetPriceHistoryByAssetIdUseCase listAssetPriceHistoryByAssetIdUseCase,
            ListAssetsUseCase listAssetsUseCase,
            CreateAssetUnityForAccountUseCase createAssetUnityForAccountUseCase
    ) {
        this.getAssetByIdUseCase = getAssetByIdUseCase;
        this.listAssetPriceHistoryByAssetIdUseCase = listAssetPriceHistoryByAssetIdUseCase;
        this.listAssetsUseCase = listAssetsUseCase;
        this.createAssetUnityForAccountUseCase = createAssetUnityForAccountUseCase;
    }

    @GetMapping("/{id}")
    public Asset getById(@PathVariable("id") long id) {
        return getAssetByIdUseCase.execute(id);
    }

    @PostMapping("/{id}/units")
    public AssetUnity createUnity(
            @PathVariable("id") long assetId,
            @RequestBody CreateAssetUnityApiRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractToken(authorization, request.token());
        return createAssetUnityForAccountUseCase.execute(
                assetId,
                request.ownerAccountId(),
                token
        );
    }

    @GetMapping
    public List<AssetSummaryResponse> list() {
        return listAssetsUseCase.execute();
    }

    @GetMapping("/{id}/price-history")
    public List<AssetPriceHistory> priceHistory(@PathVariable("id") long id) {
        return listAssetPriceHistoryByAssetIdUseCase.execute(id);
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
