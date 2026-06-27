package br.com.ale.dto;

import java.time.Instant;

public record ArtifactUnitView(
                             long artifactId,
                             long artifactUnitId,
                             String assetText,
                             Instant createdAt
) {}
