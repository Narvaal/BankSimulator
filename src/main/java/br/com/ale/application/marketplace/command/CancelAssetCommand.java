package br.com.ale.application.marketplace.command;

public record CancelAssetCommand(long assetListingId, String token) {
}
