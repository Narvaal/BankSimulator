package br.com.ale.application.marketplace.command;

public record PurchaseAssetCommand(long buyerAccountId, long listingId, String token) {
}
