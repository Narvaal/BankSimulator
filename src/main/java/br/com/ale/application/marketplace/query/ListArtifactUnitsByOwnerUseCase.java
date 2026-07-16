package br.com.ale.application.marketplace.query;

import br.com.ale.dto.ArtifactUnitPageView;
import br.com.ale.service.artifact.ArtifactUnitService;

public class ListArtifactUnitsByOwnerUseCase {

  private final ArtifactUnitService artifactUnitService;

  public ListArtifactUnitsByOwnerUseCase(ArtifactUnitService artifactUnitService) {
    this.artifactUnitService = artifactUnitService;
  }

  public ArtifactUnitPageView execute(long ownerAccountId, int page, int pageSize) {
    return artifactUnitService.selectByOwnerAccount(ownerAccountId, page, pageSize);
  }
}
