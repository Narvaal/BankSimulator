package br.com.ale.domain.artifact;

import java.time.Instant;

public class ArtifactBundle {
  private final Long id;
  private final String identifier;
  private final Instant createdAt;

  public ArtifactBundle(Long id, String identifier, Instant createdAt) {
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
