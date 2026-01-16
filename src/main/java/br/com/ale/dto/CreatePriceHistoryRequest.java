package br.com.ale.dto;

import br.com.ale.domain.asset.ReasonType;

import java.math.BigDecimal;

public record CreatePriceHistoryRequest(Long assetListingId,
                                        Long assetUnityId,
                                        BigDecimal oldPrice,
                                        BigDecimal newPrice,
                                        Long changedByAccountId,
                                        ReasonType reason) {
}
