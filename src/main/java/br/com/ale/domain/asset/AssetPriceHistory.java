package br.com.ale.domain.asset;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class AssetPriceHistory {

    private final Long id;
    private final Long assetListingId;
    private final Long assetUnityId;

    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;

    private final Long changedByAccountId;
    private final ReasonType reason;
    private final Instant createdAt;

    public AssetPriceHistory(
            Long id,
            Long assetListingId,
            Long assetUnityId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            Long changedByAccountId,
            ReasonType reason,
            Instant createdAt
    ) {
        this.id = id;
        this.assetListingId = Objects.requireNonNull(assetListingId, "assetListingId");
        this.assetUnityId = Objects.requireNonNull(assetUnityId, "assetUnityId");
        this.oldPrice = validateOldPrice(oldPrice);
        this.newPrice = validateNewPrice(newPrice);
        this.changedByAccountId = Objects.requireNonNull(changedByAccountId, "changedByAccountId");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public AssetPriceHistory(
            Long assetListingId,
            Long assetUnityId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            Long changedByAccountId,
            ReasonType reason
    ) {
        this.id = null;
        this.assetListingId = Objects.requireNonNull(assetListingId, "assetListingId");
        this.assetUnityId = Objects.requireNonNull(assetUnityId, "assetUnityId");
        this.oldPrice = validateOldPrice(oldPrice);
        this.newPrice = validateNewPrice(newPrice);
        this.changedByAccountId = Objects.requireNonNull(changedByAccountId, "changedByAccountId");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.createdAt = null;
    }

    private BigDecimal validateOldPrice(BigDecimal oldPrice) {
        if (oldPrice == null) {
            return null;
        }
        if (oldPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Asset price history oldPrice cannot be negative [oldPrice=" + oldPrice + "]"
            );
        }
        return oldPrice;
    }

    private BigDecimal validateNewPrice(BigDecimal newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("newPrice cannot be null");
        }
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Asset price history newPrice cannot be negative [newPrice=" + newPrice + "]"
            );
        }
        return newPrice;
    }

    public Long getId() {
        return id;
    }

    public Long getAssetListingId() {
        return assetListingId;
    }

    public Long getAssetUnityId() {
        return assetUnityId;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public Long getChangedByAccountId() {
        return changedByAccountId;
    }

    public ReasonType getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
