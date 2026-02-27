package br.com.ale.domain.asset;

import java.time.Instant;

public class AssetUnity {
    private final Long id;
    private final long assetId;
    private final Long ownerAccountId;
    private final AssetUnityStatus status;
    private final Instant lockedAt;
    private final Instant createdAt;

    public AssetUnity(Long id, long assetId, Long ownerAccountId, AssetUnityStatus status, Instant lockedAt,
                      Instant createdAt) {
        this.id = id;
        this.assetId = assetId;
        this.ownerAccountId = ownerAccountId;
        this.status = status;
        this.lockedAt = lockedAt;
        this.createdAt = createdAt;
    }

    public AssetUnity(long assetId, Long ownerAccountId) {
        this.id = null;
        this.assetId = assetId;
        this.ownerAccountId = ownerAccountId;
        this.status = AssetUnityStatus.AVAILABLE;
        this.lockedAt = null;
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

    public Instant getLockedAt() {
        return lockedAt;
    }

    public AssetUnityStatus getStatus() {
        return status;
    }
}
