package br.com.ale.dto;

import br.com.ale.domain.artifact.ArtifactPriceHistory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ArtifactUnitDetailView(
        long unitId,
        long artifactId,
        String artifactName,
        Map<String, Object> metadata,
        long ownerAccountId,
        String status,
        Instant createdAt,
        List<ArtifactPriceHistory> priceHistory,
        List<ArtifactUnitTransferView> transfers
) {}
