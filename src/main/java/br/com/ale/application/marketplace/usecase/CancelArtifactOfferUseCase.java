package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CancelArtifactCommand;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.JwtService;

public class CancelArtifactOfferUseCase {

  private final ArtifactListingService artifactListingService;
  private final JwtService jwtService;

  public CancelArtifactOfferUseCase(
      ArtifactListingService artifactListingService, JwtService jwtService) {
    this.artifactListingService = artifactListingService;
    this.jwtService = jwtService;
  }

  public void execute(CancelArtifactCommand command) {

    long clientId = jwtService.extractClientId(command.token());

    artifactListingService.cancelListing(command.artifactListingId(), clientId);
  }
}
