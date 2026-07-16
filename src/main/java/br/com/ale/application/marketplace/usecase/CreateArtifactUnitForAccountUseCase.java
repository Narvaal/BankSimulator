package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.CreateArtifactUnitForAccountCommand;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateArtifactUnitRequest;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;

public class CreateArtifactUnitForAccountUseCase {

  private final ArtifactUnitService artifactUnitService;
  private final JwtService jwtService;

  public CreateArtifactUnitForAccountUseCase(
      ArtifactUnitService artifactUnitService, JwtService jwtService) {
    this.artifactUnitService = artifactUnitService;
    this.jwtService = jwtService;
  }

  public ArtifactUnit execute(CreateArtifactUnitForAccountCommand command) {
    if (!jwtService.isTokenValid(command.token())) {
      throw new UnauthorizedOperationException("Authenticated client does not own this account");
    }

    return artifactUnitService.createArtifactUnit(
        new CreateArtifactUnitRequest(command.artifactId(), command.ownerAccountId()));
  }
}
