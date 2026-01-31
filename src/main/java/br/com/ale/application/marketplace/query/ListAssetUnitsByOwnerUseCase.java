package br.com.ale.application.marketplace.query;

import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.service.asset.AssetUnityService;

import java.util.List;

public class ListAssetUnitsByOwnerUseCase {

    private final AssetUnityService assetUnityService;

    public ListAssetUnitsByOwnerUseCase(AssetUnityService assetUnityService) {
        this.assetUnityService = assetUnityService;
    }

    public List<AssetUnity> execute(long ownerAccountId) {
        return assetUnityService.selectByOwnerAccount(ownerAccountId);
    }
}
