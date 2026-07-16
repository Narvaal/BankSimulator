package br.com.ale.application.marketplace.command;

import br.com.ale.dto.CreateArtifactRequest;
import java.util.List;

public record CreateArtifactBundleCommand(
    List<CreateArtifactRequest> assetRequests, String identifier) {}
