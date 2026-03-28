package br.com.ale.application.config;

import br.com.ale.application.claim.usecase.ClaimAssetUnityUseCase;
import br.com.ale.application.claim.usecase.GetNextClaimUseCase;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaimConfig {
    @Bean
    public ClaimAssetUnityUseCase ClaimAssetUnityUseCase(
            AccountService accountService,
            AssetUnityService assetUnityService,
            AssetService assetService,
            JwtService jwtService
    ) {
        return new ClaimAssetUnityUseCase(accountService, assetUnityService, assetService, jwtService);
    }

    @Bean
    GetNextClaimUseCase getNextClaimUseCase(
            AccountService accountService,
            JwtService jwtService
    ) {
        return new GetNextClaimUseCase(accountService, jwtService);
    }
}
