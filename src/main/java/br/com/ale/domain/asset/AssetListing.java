package br.com.ale.domain.asset;

import java.math.BigDecimal;
import java.time.Instant;

public class AssetListing {
    private final Long id;
    private final long assetUnitId;
    private final long sellerAccountId;
    private final BigDecimal price;
    private final AssetListingStatus status;
    private final Instant createAt;
    private final Instant updatedAt;

    public AssetListing(
            Long id, long assetUnitId, long sellerAccountId, BigDecimal price, AssetListingStatus status,
            Instant createAt, Instant updatedAt
    ) {
        this.id = id;
        this.assetUnitId = assetUnitId;
        this.sellerAccountId = sellerAccountId;
        this.price = price;
        this.status = status;
        this.createAt = createAt;
        this.updatedAt = updatedAt;

    }

    public AssetListing(
            long assetUnitId, long sellerAccountId, BigDecimal price, AssetListingStatus status
    ) {
        this.id = null;
        this.assetUnitId = assetUnitId;
        this.sellerAccountId = sellerAccountId;
        this.price = price;
        this.status = status;
        this.createAt = null;
        this.updatedAt = null;
    }

    public Long getId() {
        return id;
    }

    public long getAssetUnityId() {
        return assetUnitId;
    }

    public long getSellerAccountId() {
        return sellerAccountId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public AssetListingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
