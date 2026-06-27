package br.com.ale.application.claim.usecase;

import br.com.ale.application.claim.command.ClaimArtifactUnitCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateArtifactUnitRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

public class ClaimArtifactUnitUseCase {
    private final AccountService accountService;
    private final ArtifactUnitService artifactUnitService;
    private final ArtifactService assetService;
    private final JwtService jwtService;

    public ClaimArtifactUnitUseCase(
            AccountService accountService,
            ArtifactUnitService artifactUnitService,
            ArtifactService assetService,
            JwtService jwtService
    ) {
        this.accountService = accountService;
        this.artifactUnitService = artifactUnitService;
        this.assetService = assetService;
        this.jwtService = jwtService;
    }


    public Instant execute(ClaimArtifactUnitCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());

        Account account = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedOperationException("Account not found"));

        Artifact artifact = assetService.selectById(command.artifactId());

        Instant nextClaim = accountService
                .tryClaimArtifactUnit(account.getAccountNumber())
                .orElseThrow(() -> new UnauthorizedOperationException("nextClaim"));

        artifactUnitService.createArtifactUnit(
                new CreateArtifactUnitRequest(artifact.getId(), account.getId())
        );

        return nextClaim;
    }
}
