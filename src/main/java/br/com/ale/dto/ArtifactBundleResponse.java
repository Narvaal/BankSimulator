package br.com.ale.dto;

import java.time.Instant;

public record ArtifactBundleResponse(long id, String identifier, Instant createdAt) {
}
