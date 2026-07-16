package br.com.ale.application.marketplace.query;

import br.com.ale.dto.ArtifactTransferLogPageView;
import br.com.ale.service.artifact.ArtifactTransferService;

public class ListArtifactTransferLogUseCase {

  private final ArtifactTransferService artifactTransferService;

  public ListArtifactTransferLogUseCase(ArtifactTransferService artifactTransferService) {
    this.artifactTransferService = artifactTransferService;
  }

  public ArtifactTransferLogPageView execute(Long artifactId, int page, int pageSize) {
    if (page < 0 || pageSize <= 0) {
      throw new IllegalArgumentException("Invalid pagination params");
    }
    return artifactTransferService.publicFeed(artifactId, page, pageSize);
  }
}
