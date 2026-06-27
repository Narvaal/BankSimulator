package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateArtifactBundleCommand;
import br.com.ale.dto.ArtifactBundleResponse;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.service.artifact.ArtifactBundleService;
import java.util.List;

public class CreateArtifactBundleUseCase {

    private final ArtifactBundleService artifactBundleService;

    public CreateArtifactBundleUseCase(ArtifactBundleService artifactBundleService) {
        this.artifactBundleService = artifactBundleService;
    }

    public ArtifactBundleResponse execute(
            CreateArtifactBundleCommand command
    ) {
        return artifactBundleService.createBundle(command.assetRequests(), command.identifier());
    }
}
