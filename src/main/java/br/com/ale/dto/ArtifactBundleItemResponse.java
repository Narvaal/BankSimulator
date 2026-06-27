package br.com.ale.dto;

import java.time.Instant;

public record ArtifactBundleItemResponse(long id, String text, int totalSupply, Instant createdAt) {
}
