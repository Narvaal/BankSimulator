package br.com.ale.application.marketplace.query;

import br.com.ale.domain.account.Account;
import br.com.ale.dto.ArtifactListingFilter;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.JwtService;

public class ListActiveArtifactListingsUseCase {

    private final AccountService accountService;
    private final ArtifactListingService artifactListingService;
    private final JwtService jwtService;

    public ListActiveArtifactListingsUseCase(AccountService accountService,
                                          ArtifactListingService artifactListingService,
                                          JwtService jwtService) {
        this.accountService = accountService;
        this.artifactListingService = artifactListingService;
        this.jwtService = jwtService;
    }

    public ArtifactListingPageView execute(String token, ArtifactListingFilter filter, int page, int pageSize) {

        long accountId = -1;

        if (jwtService.isTokenValid(token)) {
            long clientId = jwtService.extractClientId(token);
            accountId = accountService.getAccountByClientId(clientId)
                    .map(Account::getId)
                    .orElse(-1L);
        }

        return artifactListingService.selectActiveByActiveStatus(accountId, filter, page, pageSize);
    }
}
