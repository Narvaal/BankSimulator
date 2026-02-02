package br.com.ale.domain.auth;

public class Credential {

    private final long id;
    private final long clientId;
    private final String email;
    private final String passwordHash;

    public Credential(
            long id,
            long clientId,
            String email,
            String passwordHash
    ) {
        this.id = id;
        this.clientId = clientId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public long getClientId() {
        return clientId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
