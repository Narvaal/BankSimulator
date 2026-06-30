package br.com.ale.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record ArtifactListingView(
        long id,
        long artifactUnitId,
        long artifactId,
        String artifactName,
        Map<String, Object> metadata,
        BigDecimal price,
        Instant createdAt
) {
}
