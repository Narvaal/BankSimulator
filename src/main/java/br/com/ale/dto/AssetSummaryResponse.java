package br.com.ale.dto;

import java.time.Instant;

public record AssetSummaryResponse(String text, int totalSupply, Instant createdAt) {
}
