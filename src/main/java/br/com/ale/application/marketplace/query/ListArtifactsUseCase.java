package br.com.ale.application.marketplace.query;

import br.com.ale.dto.ArtifactSummaryResponse;
import br.com.ale.service.artifact.ArtifactService;
import java.util.List;

public class ListArtifactsUseCase {

  private final ArtifactService assetService;

  public ListArtifactsUseCase(ArtifactService assetService) {
    this.assetService = assetService;
  }

  public List<ArtifactSummaryResponse> execute() {
    return assetService.listArtifacts();
  }
}
