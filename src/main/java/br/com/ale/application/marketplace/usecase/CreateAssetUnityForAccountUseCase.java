package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateAssetUnityForAccountCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetUnityService;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.auth.JwtService;

public class CreateAssetUnityForAccountUseCase {

    private final AssetUnityService assetUnityService;
    private final JwtService jwtService;

    public CreateAssetUnityForAccountUseCase(
            AssetUnityService assetUnityService,
            JwtService jwtService
    ) {
        this.assetUnityService = assetUnityService;
        this.jwtService = jwtService;
    }

    public AssetUnity execute(CreateAssetUnityForAccountCommand command) {
        if (!jwtService.isTokenValid(command.token())) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this account"
            );
        }

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(command.assetId(), command.ownerAccountId())
        );
    }
}
