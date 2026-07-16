package br.com.ale.service.artifact;

import br.com.ale.domain.artifact.Artifact;
import java.util.List;

public class ArtifactGenerationManager {

  private final ArtifactGenerationService assetGenerationService;
  private final ArtifactBundleService artifactBundleService;

  public ArtifactGenerationManager(
      ArtifactGenerationService assetGenerationService,
      ArtifactBundleService artifactBundleService) {
    this.assetGenerationService = assetGenerationService;
    this.artifactBundleService = artifactBundleService;
  }

  public List<Artifact> generateWeeklyAssets() {
    List<Artifact> generated = assetGenerationService.generateWeeklyAssets();
    return artifactBundleService.createWeeklyBundle(generated);
  }
}
