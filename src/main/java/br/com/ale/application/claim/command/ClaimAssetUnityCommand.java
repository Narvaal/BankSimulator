package br.com.ale.application.claim.command;

public record ClaimAssetUnityCommand(
        Long assetId,
        String token
) {
}
