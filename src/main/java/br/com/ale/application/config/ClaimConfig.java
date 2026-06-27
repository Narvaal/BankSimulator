package br.com.ale.application.config;

import br.com.ale.application.claim.usecase.ClaimArtifactUnitUseCase;
import br.com.ale.application.claim.usecase.GetNextClaimUseCase;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaimConfig {
    @Bean
    public ClaimArtifactUnitUseCase ClaimArtifactUnitUseCase(
            AccountService accountService,
            ArtifactUnitService artifactUnitService,
            ArtifactService assetService,
            JwtService jwtService
    ) {
        return new ClaimArtifactUnitUseCase(accountService, artifactUnitService, assetService, jwtService);
    }

    @Bean
    GetNextClaimUseCase getNextClaimUseCase(
            AccountService accountService,
            JwtService jwtService
    ) {
        return new GetNextClaimUseCase(accountService, jwtService);
    }
}
