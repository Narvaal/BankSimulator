package br.com.ale.domain.exception;

public class ArtifactUnitNotFoundException extends BusinessRuleException {
    public ArtifactUnitNotFoundException(long id) {
        super("Artifact unit not found [id=" + id + "]");
    }
}
