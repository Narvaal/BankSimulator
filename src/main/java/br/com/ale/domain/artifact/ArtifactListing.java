package br.com.ale.domain.artifact;

import java.math.BigDecimal;
import java.time.Instant;

public class ArtifactListing {
    private final Long id;
    private final long artifactUnitId;
    private final long sellerAccountId;
    private final BigDecimal price;
    private final ArtifactListingStatus status;
    private final Instant createAt;
    private final Instant updatedAt;

    public ArtifactListing(
            Long id, long artifactUnitId, long sellerAccountId, BigDecimal price, ArtifactListingStatus status,
            Instant createAt, Instant updatedAt
    ) {
        this.id = id;
        this.artifactUnitId = artifactUnitId;
        this.sellerAccountId = sellerAccountId;
        this.price = setPrice(price);
        this.status = status;
        this.createAt = createAt;
        this.updatedAt = updatedAt;
    }

    public ArtifactListing(
            long artifactUnitId, long sellerAccountId, BigDecimal price, ArtifactListingStatus status
    ) {
        this.id = null;
        this.artifactUnitId = artifactUnitId;
        this.sellerAccountId = sellerAccountId;
        this.price = setPrice(price);
        this.status = status;
        this.createAt = null;
        this.updatedAt = null;
    }

    private BigDecimal setPrice(BigDecimal price) {

        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (price.scale() > 2) {
            throw new IllegalArgumentException("Price cannot have more than 2 decimal places");
        }

        return price;
    }

    public Long getId() {
        return id;
    }

    public long getArtifactUnitId() {
        return artifactUnitId;
    }

    public long getSellerAccountId() {
        return sellerAccountId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ArtifactListingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
