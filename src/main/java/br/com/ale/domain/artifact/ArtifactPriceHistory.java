package br.com.ale.domain.artifact;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class ArtifactPriceHistory {

  private final Long id;
  private final Long artifactListingId;
  private final Long artifactUnitId;

  private final BigDecimal oldPrice;
  private final BigDecimal newPrice;

  private final Long changedByAccountId;
  private final ReasonType reason;
  private final Instant createdAt;

  public ArtifactPriceHistory(
      Long id,
      Long artifactListingId,
      Long artifactUnitId,
      BigDecimal oldPrice,
      BigDecimal newPrice,
      Long changedByAccountId,
      ReasonType reason,
      Instant createdAt) {
    this.id = id;
    this.artifactListingId = Objects.requireNonNull(artifactListingId, "artifactListingId");
    this.artifactUnitId = Objects.requireNonNull(artifactUnitId, "artifactUnitId");
    this.oldPrice = validateOldPrice(oldPrice);
    this.newPrice = validateNewPrice(newPrice);
    this.changedByAccountId = Objects.requireNonNull(changedByAccountId, "changedByAccountId");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public ArtifactPriceHistory(
      Long artifactListingId,
      Long artifactUnitId,
      BigDecimal oldPrice,
      BigDecimal newPrice,
      Long changedByAccountId,
      ReasonType reason) {
    this.id = null;
    this.artifactListingId = Objects.requireNonNull(artifactListingId, "artifactListingId");
    this.artifactUnitId = Objects.requireNonNull(artifactUnitId, "artifactUnitId");
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
          "Artifact price history oldPrice cannot be negative [oldPrice=" + oldPrice + "]");
    }
    return oldPrice;
  }

  private BigDecimal validateNewPrice(BigDecimal newPrice) {
    if (newPrice == null) {
      throw new IllegalArgumentException("newPrice cannot be null");
    }
    if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException(
          "Artifact price history newPrice cannot be negative [newPrice=" + newPrice + "]");
    }
    return newPrice;
  }

  public Long getId() {
    return id;
  }

  public Long getArtifactListingId() {
    return artifactListingId;
  }

  public Long getArtifactUnitId() {
    return artifactUnitId;
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
