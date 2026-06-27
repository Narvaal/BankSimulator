package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetArtifactListingByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveArtifactListingsUseCase;
import br.com.ale.application.marketplace.query.ListArtifactListingsByOwnerUseCase;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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
    public ArtifactListingPageView list(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int pageSize,
                                     HttpServletRequest request) {
        String token = authCookieService.extractTokenOrNull(request);
        return listActiveArtifactListingsUseCase.execute(token, page, pageSize);
    }

    @GetMapping("/me")
    public ArtifactListingPageView user(@RequestParam("page") int page,
                                     @RequestParam("pageSize") int pageSize,
                                     HttpServletRequest request) {
        String token = authCookieService.extractToken(request);
        return listArtifactListingsByOwnerUseCase.execute(token, page, pageSize);
    }
}
