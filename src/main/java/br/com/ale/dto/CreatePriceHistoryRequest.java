package br.com.ale.dto;

import br.com.ale.domain.artifact.ReasonType;

import java.math.BigDecimal;

public record CreatePriceHistoryRequest(Long artifactListingId,
                                        Long artifactUnitId,
                                        BigDecimal oldPrice,
                                        BigDecimal newPrice,
                                        Long changedByAccountId,
                                        ReasonType reason) {
}
