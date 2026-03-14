package br.com.ale.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AssetListingView(
        long id,
        long assetUnityId,
        long assetId,
        String assetText,
        BigDecimal price,
        Instant createdAt
) {
}
