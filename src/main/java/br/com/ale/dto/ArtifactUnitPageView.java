package br.com.ale.dto;

import java.util.List;

public record ArtifactUnitPageView(
        List<ArtifactUnitView> items,
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
