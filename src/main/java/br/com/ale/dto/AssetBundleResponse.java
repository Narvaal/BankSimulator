package br.com.ale.dto;

import java.time.Instant;

public record AssetBundleResponse(long id, String identifier, Instant createdAt) {
}
