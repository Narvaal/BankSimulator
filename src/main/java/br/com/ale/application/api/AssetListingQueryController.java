package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetAssetListingByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveAssetListingsUseCase;
import br.com.ale.application.marketplace.query.ListAssetListingsByOwnerUseCase;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.dto.AssetListingPageView;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset-listings")
public class AssetListingQueryController {

    private final AuthCookieService authCookieService;
    private final GetAssetListingByIdUseCase getAssetListingByIdUseCase;
    private final ListActiveAssetListingsUseCase listActiveAssetListingsUseCase;
    private final ListAssetListingsByOwnerUseCase listAssetListingsByOwnerUseCase;

    public AssetListingQueryController(
            AuthCookieService authCookieService,
            GetAssetListingByIdUseCase getAssetListingByIdUseCase,
            ListActiveAssetListingsUseCase listActiveAssetListingsUseCase,
            ListAssetListingsByOwnerUseCase listAssetListingsByOwnerUseCase
    ) {
        this.authCookieService = authCookieService;
        this.getAssetListingByIdUseCase = getAssetListingByIdUseCase;
        this.listActiveAssetListingsUseCase = listActiveAssetListingsUseCase;
        this.listAssetListingsByOwnerUseCase = listAssetListingsByOwnerUseCase;
    }

    @GetMapping("/{id}")
    public AssetListing getById(@PathVariable("id") long id) {
        return getAssetListingByIdUseCase.execute(id);
    }

    @GetMapping
    public AssetListingPageView list(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int pageSize,
                                     HttpServletRequest response) {
        String token = authCookieService.extractToken(response);
        return listActiveAssetListingsUseCase.execute(token, page, pageSize);
    }

    @GetMapping("me")
    public AssetListingPageView user(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int pageSize,
                                     HttpServletRequest response) {
        String token = authCookieService.extractToken(response);
        return listAssetListingsByOwnerUseCase.execute(token, page, pageSize);
    }
}
