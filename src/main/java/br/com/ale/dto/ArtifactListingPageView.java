package br.com.ale.dto;

import java.util.List;

public record ArtifactListingPageView(
        List<ArtifactListingView> items,
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
