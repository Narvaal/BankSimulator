package br.com.ale.dto;

import br.com.ale.domain.artifact.ArtifactListingStatus;

import java.math.BigDecimal;

public record CreateArtifactListingRequest(long artifactUnitId, long sellerAccountId, BigDecimal price, ArtifactListingStatus status) {
}
