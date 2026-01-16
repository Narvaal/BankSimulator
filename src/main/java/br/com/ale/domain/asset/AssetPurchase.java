package br.com.ale.domain.asset;

import java.math.BigDecimal;
import java.util.Objects;

public class AssetPurchase {

    private final long listingId;
    private final long assetUnityId;
    private final long sellerAccountId;
    private final long buyerAccountId;
    private final BigDecimal price;

    public AssetPurchase(
            long listingId,
            long assetUnityId,
            long sellerAccountId,
            long buyerAccountId,
            BigDecimal price
    ) {
        this.listingId = listingId;
        this.assetUnityId = assetUnityId;
        this.sellerAccountId = sellerAccountId;
        this.buyerAccountId = buyerAccountId;
        this.price = price;
    }

    public long getListingId() {
        return listingId;
    }

    public long getAssetUnityId() {
        return assetUnityId;
    }

    public long getSellerAccountId() {
        return sellerAccountId;
    }

    public long getBuyerAccountId() {
        return buyerAccountId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetPurchase that)) return false;
        return listingId == that.listingId
                && assetUnityId == that.assetUnityId
                && sellerAccountId == that.sellerAccountId
                && buyerAccountId == that.buyerAccountId
                && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                listingId,
                assetUnityId,
                sellerAccountId,
                buyerAccountId,
                price
        );
    }

    @Override
    public String toString() {
        return "AssetPurchase{" +
                "listingId=" + listingId +
                ", assetUnityId=" + assetUnityId +
                ", sellerAccountId=" + sellerAccountId +
                ", buyerAccountId=" + buyerAccountId +
                ", price=" + price +
                '}';
    }
}
