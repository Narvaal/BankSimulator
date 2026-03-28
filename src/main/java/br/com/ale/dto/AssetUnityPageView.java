package br.com.ale.dto;

import java.util.List;

public record AssetUnityPageView(
        List<AssetUnityView> items,
        int page,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
