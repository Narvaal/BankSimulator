package br.com.ale.dto;

import java.math.BigDecimal;

public record CreateAssetOfferApiRequest(
        long accountId,
        long assetUnityId,
        BigDecimal price,
        String token
) {
}
