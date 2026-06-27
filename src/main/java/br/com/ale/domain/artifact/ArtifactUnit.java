package br.com.ale.domain.artifact;

import java.time.Instant;

public class ArtifactUnit {
    private final Long id;
    private final long artifactId;
    private final Long ownerAccountId;
    private final ArtifactUnitStatus status;
    private final Instant lockedAt;
    private final Instant createdAt;

    public ArtifactUnit(Long id, long artifactId, Long ownerAccountId, ArtifactUnitStatus status, Instant lockedAt,
                      Instant createdAt) {
        this.id = id;
        this.artifactId = artifactId;
        this.ownerAccountId = ownerAccountId;
        this.status = status;
        this.lockedAt = lockedAt;
        this.createdAt = createdAt;
    }

    public ArtifactUnit(long artifactId, Long ownerAccountId) {
        this.id = null;
        this.artifactId = artifactId;
        this.ownerAccountId = ownerAccountId;
        this.status = ArtifactUnitStatus.AVAILABLE;
        this.lockedAt = null;
        this.createdAt = null;
    }

    public Long getId() {
        return id;
    }

    public long getArtifactId() {
        return artifactId;
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

    public ArtifactUnitStatus getStatus() {
        return status;
    }
}
