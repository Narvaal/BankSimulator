package br.com.ale.application.marketplace.command;

public record PurchaseAssetCommand(long listingId, long buyerAccountId) {
}
