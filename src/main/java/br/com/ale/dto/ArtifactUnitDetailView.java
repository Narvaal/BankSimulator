package br.com.ale.dto;

import br.com.ale.domain.artifact.ArtifactPriceHistory;

import java.time.Instant;
import java.util.List;

public record ArtifactUnitDetailView(
        long unitId,
        long artifactId,
        String artifactText,
        long ownerAccountId,
        String status,
        Instant createdAt,
        List<ArtifactPriceHistory> priceHistory,
        List<ArtifactUnitTransferView> transfers
) {}
