package br.com.ale.domain.asset;

import java.time.Instant;

public class AssetTransfer {

    private final Long id;
    private final long assetUnityId;
    private final long fromAccountId;
    private final long toAccountId;
    private final Instant createdAt;

    public AssetTransfer(
            Long id, long assetUnityId, long fromAccountId, long toAccountId, Instant createdAt
    ) {
        this.id = id;
        this.assetUnityId = validateAssetUnityId(assetUnityId);
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = createdAt;
    }

    public AssetTransfer(
            long assetUnityId, long fromAccountId, long toAccountId
    ) {
        this.id = null;
        this.assetUnityId = validateAssetUnityId(assetUnityId);
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = null;
    }

    private Long validateAssetUnityId(Long assetUnityId) {
        if (assetUnityId < 1) {
            throw new IllegalArgumentException(
                    "Asset Unity id can not be less than one [totalSupply=" + assetUnityId + "]"
            );
        }
        return assetUnityId;
    }

    public Long getId() {
        return id;
    }

    public long getAssetUnityId() {
        return assetUnityId;
    }

    public long getFromAccountId() {
        return fromAccountId;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
