package br.com.ale.dto;

import java.time.Instant;

public record AssetUnityView(
                             long assetId,
                             long assetUnityId,
                             String assetText,
                             Instant createdAt
) {}
