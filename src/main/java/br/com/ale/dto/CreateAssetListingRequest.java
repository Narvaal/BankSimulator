package br.com.ale.dto;

import br.com.ale.domain.asset.AssetListingStatus;

import java.math.BigDecimal;

public record CreateAssetListingRequest(long assetUnityId, long sellerAccountId, BigDecimal price, AssetListingStatus status) {
}
