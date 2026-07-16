package br.com.ale.dto;

import java.util.List;

public record CreateArtifactBundleApiRequest(
    List<CreateArtifactRequest> assets, String identifier) {}
