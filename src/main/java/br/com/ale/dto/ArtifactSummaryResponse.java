package br.com.ale.dto;

import java.time.Instant;

public record ArtifactSummaryResponse(String text, int totalSupply, Instant createdAt) {
}
