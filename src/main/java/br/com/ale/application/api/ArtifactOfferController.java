package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CancelArtifactCommand;
import br.com.ale.application.marketplace.command.CreateArtifactOfferCommand;
import br.com.ale.application.marketplace.usecase.CancelArtifactOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactOfferUseCase;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.dto.CancelArtifactOfferApiRequest;
import br.com.ale.dto.CreateArtifactOfferApiRequest;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artifact-offers")
public class ArtifactOfferController {

    private final CancelArtifactOfferUseCase cancelArtifactOfferUseCase;
    private final CreateArtifactOfferUseCase createArtifactOfferUseCase;
    private final AuthCookieService authCookieService;

    public ArtifactOfferController(CancelArtifactOfferUseCase cancelArtifactOfferUseCase,
                                CreateArtifactOfferUseCase createArtifactOfferUseCase,
                                AuthCookieService authCookieService) {
        this.cancelArtifactOfferUseCase = cancelArtifactOfferUseCase;
        this.createArtifactOfferUseCase = createArtifactOfferUseCase;
        this.authCookieService = authCookieService;
    }

    @PostMapping
    public ArtifactListing create(
            @RequestBody CreateArtifactOfferApiRequest body,
            HttpServletRequest httpRequest
    ) {
        String token = authCookieService.extractToken(httpRequest);

        CreateArtifactOfferCommand command =
                new CreateArtifactOfferCommand(
                        body.artifactUnitId(),
                        body.price(),
                        token
                );

        return createArtifactOfferUseCase.execute(command);
    }

    @PostMapping("/cancel")
    public void cancel(
            @RequestBody CancelArtifactOfferApiRequest body,
            HttpServletRequest httpRequest
    ) {
        String token = authCookieService.extractToken(httpRequest);

        cancelArtifactOfferUseCase.execute(
                new CancelArtifactCommand(
                        body.artifactListingId(),
                        token
                )
        );
    }
}
