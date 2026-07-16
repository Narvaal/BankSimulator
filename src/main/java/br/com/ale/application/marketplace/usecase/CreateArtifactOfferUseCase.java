package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateArtifactOfferCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateArtifactListingRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.JwtService;

public class CreateArtifactOfferUseCase {

  private final ArtifactListingService artifactListingService;
  private final AccountService accountService;
  private final JwtService jwtService;

  public CreateArtifactOfferUseCase(
      ArtifactListingService artifactListingService,
      AccountService accountService,
      JwtService jwtService) {
    this.artifactListingService = artifactListingService;
    this.accountService = accountService;
    this.jwtService = jwtService;
  }

  public ArtifactListing execute(CreateArtifactOfferCommand command) {

    if (!jwtService.isTokenValid(command.token())) {
      throw new UnauthorizedOperationException("Invalid or expired token");
    }

    long clientId = jwtService.extractClientId(command.token());

    Account account =
        accountService
            .getAccountByClientId(clientId)
            .orElseThrow(() -> new InvalidCredentialsException("Client not found"));

    return artifactListingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            command.artifactUnitId(),
            account.getId(),
            command.price(),
            ArtifactListingStatus.ACTIVE));
  }
}
