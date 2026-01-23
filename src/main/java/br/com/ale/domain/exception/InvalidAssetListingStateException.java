package br.com.ale.domain.exception;

public class InvalidAssetListingStateException extends BusinessRuleException {
    public InvalidAssetListingStateException(long listingId) {
        super("Listing not active [id=" + listingId + "]");
    }
}

