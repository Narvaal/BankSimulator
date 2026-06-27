package br.com.ale.domain.artifact;

import java.time.Instant;

public class ArtifactTransfer {

    private final Long id;
    private final long artifactUnitId;
    private final long fromAccountId;
    private final long toAccountId;
    private final Instant createdAt;

    public ArtifactTransfer(
            Long id, long artifactUnitId, long fromAccountId, long toAccountId, Instant createdAt
    ) {
        this.id = id;
        this.artifactUnitId = validateArtifactUnitId(artifactUnitId);
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = createdAt;
    }

    public ArtifactTransfer(
            long artifactUnitId, long fromAccountId, long toAccountId
    ) {
        this.id = null;
        this.artifactUnitId = validateArtifactUnitId(artifactUnitId);
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.createdAt = null;
    }

    private Long validateArtifactUnitId(Long artifactUnitId) {
        if (artifactUnitId < 1) {
            throw new IllegalArgumentException(
                    "Artifact Unity id can not be less than one [totalSupply=" + artifactUnitId + "]"
            );
        }
        return artifactUnitId;
    }

    public Long getId() {
        return id;
    }

    public long getArtifactUnitId() {
        return artifactUnitId;
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
