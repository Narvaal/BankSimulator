package br.com.ale.application.marketplace.query;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.JwtService;

public class ListArtifactListingsByOwnerUseCase {

  private final AccountService accountService;
  private final ArtifactListingService artifactListingService;
  private final JwtService jwtService;

  public ListArtifactListingsByOwnerUseCase(
      AccountService accountService,
      ArtifactListingService artifactListingService,
      JwtService jwtService) {
    this.accountService = accountService;
    this.artifactListingService = artifactListingService;
    this.jwtService = jwtService;
  }

  public ArtifactListingPageView execute(String token, int page, int pageSize) {

    if (!jwtService.isTokenValid(token)) {
      throw new UnauthorizedOperationException("Token is not valid");
    }

    long clientId = jwtService.extractClientId(token);

    Account account =
        accountService
            .getAccountByClientId(clientId)
            .orElseThrow(() -> new InvalidCredentialsException("Client not found"));

    return artifactListingService.selectByOwnerAccount(account.getId(), page, pageSize);
  }
}
