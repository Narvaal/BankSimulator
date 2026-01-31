package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetAssetListingByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveAssetListingsUseCase;
import br.com.ale.application.marketplace.query.ListAssetListingsByOwnerUseCase;
import br.com.ale.domain.asset.AssetListing;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-listings")
public class AssetListingQueryController {

    private final GetAssetListingByIdUseCase getAssetListingByIdUseCase;
    private final ListActiveAssetListingsUseCase listActiveAssetListingsUseCase;
    private final ListAssetListingsByOwnerUseCase listAssetListingsByOwnerUseCase;

    public AssetListingQueryController(
            GetAssetListingByIdUseCase getAssetListingByIdUseCase,
            ListActiveAssetListingsUseCase listActiveAssetListingsUseCase,
            ListAssetListingsByOwnerUseCase listAssetListingsByOwnerUseCase
    ) {
        this.getAssetListingByIdUseCase = getAssetListingByIdUseCase;
        this.listActiveAssetListingsUseCase = listActiveAssetListingsUseCase;
        this.listAssetListingsByOwnerUseCase = listAssetListingsByOwnerUseCase;
    }

    @GetMapping("/{id}")
    public AssetListing getById(@PathVariable("id") long id) {
        return getAssetListingByIdUseCase.execute(id);
    }

    @GetMapping
    public List<AssetListing> list(@RequestParam(value = "ownerId", required = false) Long ownerId) {
        if (ownerId == null) {
            return listActiveAssetListingsUseCase.execute();
        }
        return listAssetListingsByOwnerUseCase.execute(ownerId);
    }
}
