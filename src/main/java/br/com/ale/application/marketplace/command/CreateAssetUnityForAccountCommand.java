package br.com.ale.application.marketplace.command;

public record CreateAssetUnityForAccountCommand(
        long assetId, long ownerAccountId, String token
) {
}
