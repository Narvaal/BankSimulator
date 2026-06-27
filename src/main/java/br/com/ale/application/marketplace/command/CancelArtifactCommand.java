package br.com.ale.application.marketplace.command;

public record CancelArtifactCommand(long artifactListingId, String token) {
}
