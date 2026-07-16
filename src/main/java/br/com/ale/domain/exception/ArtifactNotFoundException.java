package br.com.ale.domain.exception;

public class ArtifactNotFoundException extends BusinessRuleException {
  public ArtifactNotFoundException(long id) {
    super("Artifact not found [id=" + id + "]");
  }
}
