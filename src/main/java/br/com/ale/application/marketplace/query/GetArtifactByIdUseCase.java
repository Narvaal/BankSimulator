package br.com.ale.application.marketplace.query;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.service.artifact.ArtifactService;

public class GetArtifactByIdUseCase {

  private final ArtifactService assetService;

  public GetArtifactByIdUseCase(ArtifactService assetService) {
    this.assetService = assetService;
  }

  public Artifact execute(long artifactId) {
    return assetService.selectById(artifactId);
  }
}
