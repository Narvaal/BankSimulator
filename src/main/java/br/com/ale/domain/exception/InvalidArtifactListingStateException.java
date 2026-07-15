package br.com.ale.domain.exception;

public class InvalidArtifactListingStateException extends BusinessRuleException {
    public InvalidArtifactListingStateException(long listingId) {
        super("Listing not active [id=" + listingId + "]");
    }
}
