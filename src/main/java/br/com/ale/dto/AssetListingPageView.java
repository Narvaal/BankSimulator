package br.com.ale.dto;

import java.util.List;

public record AssetListingPageView(
        List<AssetListingView> items,
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
