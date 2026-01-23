package br.com.ale.domain.exception;

public class AssetNotFoundException extends BusinessRuleException {
    public AssetNotFoundException(long id) {
        super("Asset not found [id=" + id + "]");
    }
}
