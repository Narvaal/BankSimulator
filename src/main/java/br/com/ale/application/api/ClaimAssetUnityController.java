package br.com.ale.application.api;

import br.com.ale.application.claim.command.ClaimAssetUnityCommand;
import br.com.ale.application.claim.command.GetNextClaimCommand;
import br.com.ale.application.claim.usecase.ClaimAssetUnityUseCase;
import br.com.ale.application.claim.usecase.GetNextClaimUseCase;
import br.com.ale.dto.ClaimAssetUnityApiRequest;
import br.com.ale.dto.ClaimAssetUnityApiResponse;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/assets/claim")
public class ClaimAssetUnityController {

    private final AuthCookieService authCookieService;
    private final ClaimAssetUnityUseCase claimAssetUnityUseCase;
    private final GetNextClaimUseCase getNextClaimUseCase;

    public ClaimAssetUnityController(
            AuthCookieService authCookieService,
            ClaimAssetUnityUseCase claimAssetUnityUseCase,
            GetNextClaimUseCase getNextClaimUseCase
    ) {
        this.authCookieService = authCookieService;
        this.claimAssetUnityUseCase = claimAssetUnityUseCase;
        this.getNextClaimUseCase = getNextClaimUseCase;
    }

    @PostMapping
    public ClaimAssetUnityApiResponse claimAssetUnity(
            @RequestBody ClaimAssetUnityApiRequest body,
            HttpServletRequest httpRequest
    ) {

        String token = authCookieService.extractToken(httpRequest);

        return new ClaimAssetUnityApiResponse(
                claimAssetUnityUseCase.execute(
                        new ClaimAssetUnityCommand(body.assetId(), token)
                )
        );
    }

    @GetMapping
    public Instant getNextClaim(HttpServletRequest httpRequest) {

        String token = authCookieService.extractToken(httpRequest);

        return getNextClaimUseCase.execute(new GetNextClaimCommand(token));
    }
}
