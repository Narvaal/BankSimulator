package br.com.ale.application.marketplace.query;

import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import java.util.List;

public class ListArtifactPriceHistoryByListingUseCase {

    private final ArtifactPriceHistoryService artifactPriceHistoryService;

    public ListArtifactPriceHistoryByListingUseCase(
            ArtifactPriceHistoryService artifactPriceHistoryService
    ) {
        this.artifactPriceHistoryService = artifactPriceHistoryService;
    }

    public List<ArtifactPriceHistory> execute(long artifactListingId) {
        return artifactPriceHistoryService.listByArtifactListingId(artifactListingId);
    }
}
