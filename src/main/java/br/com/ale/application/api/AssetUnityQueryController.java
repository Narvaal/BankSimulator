package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.ListAssetUnitsByOwnerUseCase;
import br.com.ale.domain.asset.AssetUnity;
import java.util.List;

import br.com.ale.dto.AssetUnityPageView;
import br.com.ale.dto.AssetUnityView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-units")
public class AssetUnityQueryController {

    private final ListAssetUnitsByOwnerUseCase listAssetUnitsByOwnerUseCase;

    public AssetUnityQueryController(ListAssetUnitsByOwnerUseCase listAssetUnitsByOwnerUseCase) {
        this.listAssetUnitsByOwnerUseCase = listAssetUnitsByOwnerUseCase;
    }

    @GetMapping
    public AssetUnityPageView listByOwner(@RequestParam("ownerId") long ownerId,
                                          @RequestParam("page") int page,
                                          @RequestParam("pageSize") int pageSize) {
        return listAssetUnitsByOwnerUseCase.execute(ownerId, page, pageSize);
    }
}
