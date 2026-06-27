package br.com.ale.dto;

import java.math.BigDecimal;

public record CreateArtifactOfferApiRequest(
        long accountId,
        long artifactUnitId,
        BigDecimal price,
        String token
) {
}
