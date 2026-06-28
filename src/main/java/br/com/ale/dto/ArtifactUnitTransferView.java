package br.com.ale.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ArtifactUnitTransferView(
        long id,
        long fromAccountId,
        long toAccountId,
        BigDecimal salePrice,
        Instant createdAt
) {}
