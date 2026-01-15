package br.com.ale.domain.asset;

import java.time.Instant;

public class AssetUnity {
    private final Long id;
    private final long assetId;
    private final Long ownerAccountId;
    private final Instant createdAt;

    public AssetUnity(Long id, long assetId, Long ownerAccountId, Instant createdAt) {
        this.id = id;
        this.assetId = assetId;
        this.ownerAccountId = ownerAccountId;
        this.createdAt = createdAt;
    }

    public AssetUnity(long assetId, Long ownerAccountId) {
        this.id = null;
        this.assetId = assetId;
        this.ownerAccountId = ownerAccountId;
        this.createdAt = null;
    }

    public Long getId() {
        return id;
    }

    public long getAssetId() {
        return assetId;
    }

    public Long getOwnerAccountId() {
        return ownerAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
