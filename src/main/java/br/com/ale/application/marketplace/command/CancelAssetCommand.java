package br.com.ale.application.marketplace.command;

public record CancelAssetCommand(long accountId, long assetListingId, String token) {
}
