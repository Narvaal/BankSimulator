package br.com.ale.domain.asset;

import java.time.Instant;

public class AssetBundle {
    private final Long id;
    private final String identifier;
    private final Instant createdAt;

    public AssetBundle(Long id, String identifier, Instant createdAt) {
        this.id = id;
        this.identifier = identifier;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
