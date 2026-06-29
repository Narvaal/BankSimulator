package br.com.ale.dto;

import java.math.BigDecimal;

public record ArtifactListingFilter(
        Long artifactId,
        String search,
        String sort,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
    public static ArtifactListingFilter empty() {
        return new ArtifactListingFilter(null, null, "newest", null, null);
    }
}
