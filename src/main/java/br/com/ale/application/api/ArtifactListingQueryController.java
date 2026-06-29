package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetArtifactListingByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveArtifactListingsUseCase;
import br.com.ale.application.marketplace.query.ListArtifactListingsByOwnerUseCase;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.dto.ArtifactListingFilter;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/artifact-listings")
public class ArtifactListingQueryController {

    private final AuthCookieService authCookieService;
    private final GetArtifactListingByIdUseCase getArtifactListingByIdUseCase;
    private final ListActiveArtifactListingsUseCase listActiveArtifactListingsUseCase;
    private final ListArtifactListingsByOwnerUseCase listArtifactListingsByOwnerUseCase;

    public ArtifactListingQueryController(
            AuthCookieService authCookieService,
            GetArtifactListingByIdUseCase getArtifactListingByIdUseCase,
            ListActiveArtifactListingsUseCase listActiveArtifactListingsUseCase,
            ListArtifactListingsByOwnerUseCase listArtifactListingsByOwnerUseCase
    ) {
        this.authCookieService = authCookieService;
        this.getArtifactListingByIdUseCase = getArtifactListingByIdUseCase;
        this.listActiveArtifactListingsUseCase = listActiveArtifactListingsUseCase;
        this.listArtifactListingsByOwnerUseCase = listArtifactListingsByOwnerUseCase;
    }

    @GetMapping("/{id}")
    public ArtifactListing getById(@PathVariable("id") long id) {
        return getArtifactListingByIdUseCase.execute(id);
    }

    @GetMapping("")
    public ArtifactListingPageView list(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam(required = false) Long artifactId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            HttpServletRequest request
    ) {
        String token = authCookieService.extractTokenOrNull(request);
        ArtifactListingFilter filter = new ArtifactListingFilter(artifactId, q, sort, minPrice, maxPrice);
        return listActiveArtifactListingsUseCase.execute(token, filter, page, pageSize);
    }

    @GetMapping("/me")
    public ArtifactListingPageView user(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int pageSize,
                                     HttpServletRequest request) {
        String token = authCookieService.extractToken(request);
        return listArtifactListingsByOwnerUseCase.execute(token, page, pageSize);
    }
}
