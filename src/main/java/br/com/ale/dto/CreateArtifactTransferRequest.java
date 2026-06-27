package br.com.ale.dto;

public record CreateArtifactTransferRequest(long artifactUnitId, long fromAccountId, long toAccountId) {
}
