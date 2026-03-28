package br.com.ale.application.marketplace.query;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.AssetListingPageView;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.auth.JwtService;

public class ListActiveAssetListingsUseCase {

    private final AccountService accountService;
    private final AssetListingService assetListingService;
    private final JwtService jwtService;

    public ListActiveAssetListingsUseCase(AccountService accountService,
                                          AssetListingService assetListingService,
                                          JwtService jwtService) {
        this.accountService = accountService;
        this.assetListingService = assetListingService;
        this.jwtService = jwtService;
    }

    public AssetListingPageView execute(String token, int page, int pageSize) {

        if (!jwtService.isTokenValid(token)) {
            throw new UnauthorizedOperationException("Token is not valid");
        }

        long clientId = jwtService.extractClientId(token);

        Account account = accountService.getAccountByClientId(clientId).orElseThrow(
                () -> new InvalidCredentialsException("Client not found")
        );

        return assetListingService.selectActiveByActiveStatus(account.getId(), page, pageSize);
    }
}
