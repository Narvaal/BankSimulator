package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import java.util.List;

public class ListAssetPriceHistoryByListingUseCase {

    private final AssetPriceHistoryService assetPriceHistoryService;

    public ListAssetPriceHistoryByListingUseCase(
            AssetPriceHistoryService assetPriceHistoryService
    ) {
        this.assetPriceHistoryService = assetPriceHistoryService;
    }

    public List<AssetPriceHistory> execute(long assetListingId) {
        return assetPriceHistoryService.listByAssetListingId(assetListingId);
    }
}
