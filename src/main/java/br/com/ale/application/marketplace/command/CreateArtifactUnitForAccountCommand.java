package br.com.ale.application.marketplace.command;

public record CreateArtifactUnitForAccountCommand(
        long artifactId, long ownerAccountId, String token
) {
}
