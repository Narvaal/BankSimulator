package br.com.ale.application.marketplace.query;

import br.com.ale.dto.ArtifactBundleResponse;
import br.com.ale.service.artifact.ArtifactBundleService;
import java.util.List;

public class ListArtifactBundlesUseCase {

  private final ArtifactBundleService artifactBundleService;

  public ListArtifactBundlesUseCase(ArtifactBundleService artifactBundleService) {
    this.artifactBundleService = artifactBundleService;
  }

  public List<ArtifactBundleResponse> execute(int page, int size) {
    return artifactBundleService.listBundles(page, size);
  }
}
