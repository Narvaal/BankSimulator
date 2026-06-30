package br.com.ale.domain.artifact;

import java.time.Instant;
import java.util.Map;

public class Artifact {

    private final Long id;
    private final Map<String, Object> metadata;
    private final int totalSupply;
    private final Instant createdAt;

    public Artifact(Long id, Map<String, Object> metadata, int totalSupply, Instant createdAt) {
        this.id = id;
        this.metadata = validateMetadata(metadata);
        this.totalSupply = validateTotalSupply(totalSupply);
        this.createdAt = createdAt;
    }

    public Artifact(Map<String, Object> metadata, int totalSupply) {
        this(null, metadata, totalSupply, null);
    }

    private Map<String, Object> validateMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Artifact metadata cannot be null");
        }
        Object name = metadata.get("name");
        if (name == null || name.toString().isBlank()) {
            throw new IllegalArgumentException("Artifact metadata must contain a non-blank 'name' field");
        }
        return metadata;
    }

    private int validateTotalSupply(int totalSupply) {
        if (totalSupply < 0) {
            throw new IllegalArgumentException(
                    "Artifact total supply cannot be less than zero [totalSupply=" + totalSupply + "]"
            );
        }
        return totalSupply;
    }

    public Long getId() { return id; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getName() { return (String) metadata.get("name"); }
    public int getTotalSupply() { return totalSupply; }
    public Instant getCreatedAt() { return createdAt; }
}
