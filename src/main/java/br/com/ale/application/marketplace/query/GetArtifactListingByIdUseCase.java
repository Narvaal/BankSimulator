package br.com.ale.application.marketplace.query;

import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.service.artifact.ArtifactListingService;

public class GetArtifactListingByIdUseCase {

  private final ArtifactListingService artifactListingService;

  public GetArtifactListingByIdUseCase(ArtifactListingService artifactListingService) {
    this.artifactListingService = artifactListingService;
  }

  public ArtifactListing execute(long listingId) {
    return artifactListingService.selectById(listingId);
  }
}
