package br.com.ale.domain.artifact;

import java.math.BigDecimal;
import java.util.Objects;

public class ArtifactPurchase {

  private final long listingId;
  private final long artifactUnitId;
  private final long sellerAccountId;
  private final long buyerAccountId;
  private final BigDecimal price;

  public ArtifactPurchase(
      long listingId,
      long artifactUnitId,
      long sellerAccountId,
      long buyerAccountId,
      BigDecimal price) {
    this.listingId = listingId;
    this.artifactUnitId = artifactUnitId;
    this.sellerAccountId = sellerAccountId;
    this.buyerAccountId = buyerAccountId;
    this.price = price;
  }

  public long getListingId() {
    return listingId;
  }

  public long getArtifactUnitId() {
    return artifactUnitId;
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
    if (!(o instanceof ArtifactPurchase that)) return false;
    return listingId == that.listingId
        && artifactUnitId == that.artifactUnitId
        && sellerAccountId == that.sellerAccountId
        && buyerAccountId == that.buyerAccountId
        && Objects.equals(price, that.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(listingId, artifactUnitId, sellerAccountId, buyerAccountId, price);
  }

  @Override
  public String toString() {
    return "ArtifactPurchase{"
        + "listingId="
        + listingId
        + ", artifactUnitId="
        + artifactUnitId
        + ", sellerAccountId="
        + sellerAccountId
        + ", buyerAccountId="
        + buyerAccountId
        + ", price="
        + price
        + '}';
  }
}
