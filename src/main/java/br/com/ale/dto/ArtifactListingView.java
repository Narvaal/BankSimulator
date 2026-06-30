package br.com.ale.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ArtifactListingView(
        long id,
        long artifactUnitId,
        long artifactId,
        String artifactName,
        BigDecimal price,
        Instant createdAt
) {
}
