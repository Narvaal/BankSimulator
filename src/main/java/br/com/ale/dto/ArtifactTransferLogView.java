package br.com.ale.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ArtifactTransferLogView(
        long id,
        String artifactName,
        long artifactUnitId,
        BigDecimal salePrice,
        long fromAccountId,
        long toAccountId,
        Instant createdAt
) {}
