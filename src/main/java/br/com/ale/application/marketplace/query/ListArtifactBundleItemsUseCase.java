package br.com.ale.application.marketplace.query;

import br.com.ale.dto.ArtifactBundleItemResponse;
import br.com.ale.service.artifact.ArtifactBundleService;
import java.util.List;

public class ListArtifactBundleItemsUseCase {

    private final ArtifactBundleService artifactBundleService;

    public ListArtifactBundleItemsUseCase(ArtifactBundleService artifactBundleService) {
        this.artifactBundleService = artifactBundleService;
    }

    public List<ArtifactBundleItemResponse> execute(long bundleId, int page, int size) {
        return artifactBundleService.listBundleItems(bundleId, page, size);
    }
}
