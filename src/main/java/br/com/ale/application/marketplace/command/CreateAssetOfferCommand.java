package br.com.ale.application.marketplace.command;

import java.math.BigDecimal;

public record CreateAssetOfferCommand(long assetUnityId, BigDecimal price, String token) {
}
