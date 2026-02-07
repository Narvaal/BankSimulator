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

public class CreateAssetUnityForAccountUseCase {

    private final AssetUnityService assetUnityService;
    private final AccountService accountService;
    private final AuthService authService;

    public CreateAssetUnityForAccountUseCase(
            AssetUnityService assetUnityService,
            AccountService accountService,
            AuthService authService
    ) {
        this.assetUnityService = assetUnityService;
        this.accountService = accountService;
        this.authService = authService;
    }

    public AssetUnity execute(CreateAssetUnityForAccountCommand command) {
        TokenClaims claims = authService.validateToken(command.token());
        Account account = accountService.getAccountById(command.ownerAccountId());

        if (claims.clientId() != account.getClientId()) {
            throw new UnauthorizedOperationException(
                    "Authenticated client does not own this account"
            );
        }

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(command.assetId(), command.ownerAccountId())
        );
    }
}
