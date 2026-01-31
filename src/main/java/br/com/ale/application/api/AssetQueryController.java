package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetAssetByIdUseCase;
import br.com.ale.application.marketplace.query.ListAssetPriceHistoryByAssetIdUseCase;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetPriceHistory;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets")
public class AssetQueryController {

    private final GetAssetByIdUseCase getAssetByIdUseCase;
    private final ListAssetPriceHistoryByAssetIdUseCase listAssetPriceHistoryByAssetIdUseCase;

    public AssetQueryController(
            GetAssetByIdUseCase getAssetByIdUseCase,
            ListAssetPriceHistoryByAssetIdUseCase listAssetPriceHistoryByAssetIdUseCase
    ) {
        this.getAssetByIdUseCase = getAssetByIdUseCase;
        this.listAssetPriceHistoryByAssetIdUseCase = listAssetPriceHistoryByAssetIdUseCase;
    }

    @GetMapping("/{id}")
    public Asset getById(@PathVariable("id") long id) {
        return getAssetByIdUseCase.execute(id);
    }

    @GetMapping("/{id}/price-history")
    public List<AssetPriceHistory> priceHistory(@PathVariable("id") long id) {
        return listAssetPriceHistoryByAssetIdUseCase.execute(id);
    }
}
