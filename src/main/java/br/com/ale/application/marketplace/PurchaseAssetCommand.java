package br.com.ale.application.marketplace;

public record PurchaseAssetCommand(long listingId, long buyerAccountId) {
}
