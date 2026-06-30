package br.com.ale.dto;

import java.time.Instant;
import java.util.Map;

public record ArtifactSummaryResponse(Map<String, Object> metadata, int totalSupply, Instant createdAt) {
}
