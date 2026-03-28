package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.AssetUnityPageView;
import br.com.ale.dto.AssetUnityView;
import br.com.ale.service.asset.AssetUnityService;

import java.util.List;

public class ListAssetUnitsByOwnerUseCase {

    private final AssetUnityService assetUnityService;

    public ListAssetUnitsByOwnerUseCase(AssetUnityService assetUnityService) {
        this.assetUnityService = assetUnityService;
    }

    public AssetUnityPageView execute(long ownerAccountId, int page, int pageSize) {
        return assetUnityService.selectByOwnerAccount(ownerAccountId, page, pageSize);
    }
}
