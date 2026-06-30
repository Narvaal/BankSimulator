package br.com.ale.dto;

import java.time.Instant;
import java.util.Map;

public record ArtifactUnitView(
        long artifactId,
        long artifactUnitId,
        String artifactName,
        Map<String, Object> metadata,
        Instant createdAt
) {}
