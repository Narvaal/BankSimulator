package br.com.ale.dto;

public record CreateAssetTransferRequest(long assetUnityId, long fromAccountId, long toAccountId) {
}
