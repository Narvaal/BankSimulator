package br.com.ale.domain.auth;

public class Credential {

    private final long id;
    private final long clientId;
    private final String document;
    private final String passwordHash;

    public Credential(
            long id,
            long clientId,
            String document,
            String passwordHash
    ) {
        this.id = id;
        this.clientId = clientId;
        this.document = document;
        this.passwordHash = passwordHash;
    }

    public long getClientId() {
        return clientId;
    }

    public String getDocument() {
        return document;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
