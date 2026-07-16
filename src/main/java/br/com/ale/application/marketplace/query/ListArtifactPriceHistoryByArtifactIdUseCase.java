package br.com.ale.application.marketplace.query;

import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import java.util.List;

public class ListArtifactPriceHistoryByArtifactIdUseCase {

  private final ArtifactPriceHistoryService artifactPriceHistoryService;

  public ListArtifactPriceHistoryByArtifactIdUseCase(
      ArtifactPriceHistoryService artifactPriceHistoryService) {
    this.artifactPriceHistoryService = artifactPriceHistoryService;
  }

  public List<ArtifactPriceHistory> execute(long artifactId) {
    return artifactPriceHistoryService.listByArtifactId(artifactId);
  }
}
