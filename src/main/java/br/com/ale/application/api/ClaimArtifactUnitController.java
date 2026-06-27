package br.com.ale.application.api;

import br.com.ale.application.claim.command.ClaimArtifactUnitCommand;
import br.com.ale.application.claim.command.GetNextClaimCommand;
import br.com.ale.application.claim.usecase.ClaimArtifactUnitUseCase;
import br.com.ale.application.claim.usecase.GetNextClaimUseCase;
import br.com.ale.dto.ClaimArtifactUnitApiRequest;
import br.com.ale.dto.ClaimArtifactUnitApiResponse;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/artifacts/claim")
public class ClaimArtifactUnitController {

    private final AuthCookieService authCookieService;
    private final ClaimArtifactUnitUseCase claimArtifactUnitUseCase;
    private final GetNextClaimUseCase getNextClaimUseCase;

    public ClaimArtifactUnitController(
            AuthCookieService authCookieService,
            ClaimArtifactUnitUseCase claimArtifactUnitUseCase,
            GetNextClaimUseCase getNextClaimUseCase
    ) {
        this.authCookieService = authCookieService;
        this.claimArtifactUnitUseCase = claimArtifactUnitUseCase;
        this.getNextClaimUseCase = getNextClaimUseCase;
    }

    @PostMapping
    public ClaimArtifactUnitApiResponse claimArtifactUnit(
            @RequestBody ClaimArtifactUnitApiRequest body,
            HttpServletRequest httpRequest
    ) {

        String token = authCookieService.extractToken(httpRequest);

        return new ClaimArtifactUnitApiResponse(
                claimArtifactUnitUseCase.execute(
                        new ClaimArtifactUnitCommand(body.artifactId(), token)
                )
        );
    }

    @GetMapping
    public Instant getNextClaim(HttpServletRequest httpRequest) {

        String token = authCookieService.extractToken(httpRequest);

        return getNextClaimUseCase.execute(new GetNextClaimCommand(token));
    }
}
