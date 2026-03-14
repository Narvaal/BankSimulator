package br.com.ale.application.claim.usecase;

import br.com.ale.application.claim.command.ClaimAssetUnityCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.JwtService;

import java.time.Instant;

public class ClaimAssetUnityUseCase {
    private final AccountService accountService;
    private final AssetUnityService assetUnityService;
    private final AssetService assetService;
    private final JwtService jwtService;

    public ClaimAssetUnityUseCase(
            AccountService accountService,
            AssetUnityService assetUnityService,
            AssetService assetService,
            JwtService jwtService
    ) {
        this.accountService = accountService;
        this.assetUnityService = assetUnityService;
        this.assetService = assetService;
        this.jwtService = jwtService;
    }


    public Instant execute(ClaimAssetUnityCommand command) {

        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException("Invalid or expired token");
        }

        long clientId = jwtService.extractClientId(command.token());

        Account account = accountService.getAccountByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedOperationException("Account not found"));

        Asset asset = assetService.selectById(command.assetId());

        Instant nextClaim = accountService
                .tryClaimAssetUnity(account.getAccountNumber())
                .orElseThrow(() -> new UnauthorizedOperationException("nextClaim"));

        assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(asset.getId(), account.getId())
        );

        return nextClaim;
    }
}
