package br.com.ale.application.marketplace.query;

import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.dto.ArtifactUnitDetailView;
import br.com.ale.dto.ArtifactUnitTransferView;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactTransferService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;

import java.util.List;

public class GetArtifactUnitByIdUseCase {

    private final ArtifactUnitService artifactUnitService;
    private final ArtifactService artifactService;
    private final ArtifactPriceHistoryService priceHistoryService;
    private final ArtifactTransferService transferService;

    public GetArtifactUnitByIdUseCase(
            ArtifactUnitService artifactUnitService,
            ArtifactService artifactService,
            ArtifactPriceHistoryService priceHistoryService,
            ArtifactTransferService transferService
    ) {
        this.artifactUnitService = artifactUnitService;
        this.artifactService = artifactService;
        this.priceHistoryService = priceHistoryService;
        this.transferService = transferService;
    }

    public ArtifactUnitDetailView execute(long unitId) {
        ArtifactUnit unit = artifactUnitService.selectById(unitId);
        String artifactText = artifactService.selectById(unit.getArtifactId()).getText();
        List<ArtifactPriceHistory> priceHistory = priceHistoryService.listByArtifactId(unitId);
        List<ArtifactUnitTransferView> transfers = transferService.selectByUnitId(unitId);

        return new ArtifactUnitDetailView(
                unit.getId(),
                unit.getArtifactId(),
                artifactText,
                unit.getOwnerAccountId(),
                unit.getStatus().name(),
                unit.getCreatedAt(),
                priceHistory,
                transfers
        );
    }
}
