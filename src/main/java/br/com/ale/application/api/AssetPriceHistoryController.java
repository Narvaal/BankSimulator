package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.ListAssetPriceHistoryByListingUseCase;
import br.com.ale.domain.asset.AssetPriceHistory;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-listings")
public class AssetPriceHistoryController {

    private final ListAssetPriceHistoryByListingUseCase listAssetPriceHistoryByListingUseCase;

    public AssetPriceHistoryController(
            ListAssetPriceHistoryByListingUseCase listAssetPriceHistoryByListingUseCase
    ) {
        this.listAssetPriceHistoryByListingUseCase = listAssetPriceHistoryByListingUseCase;
    }

    @GetMapping("/{id}/price-history")
    public List<AssetPriceHistory> listByListing(@PathVariable("id") long id) {
        return listAssetPriceHistoryByListingUseCase.execute(id);
    }
}
