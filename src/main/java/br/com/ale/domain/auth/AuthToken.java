package br.com.ale.domain.auth;

import java.time.Instant;

public class AuthToken {
    private final long clientId;
    private final String token;
    private final Instant date;


    public AuthToken(
            long clientID,
            String email,
            Instant date
    ) {
        this.clientId = clientID;
        this.token = validateToken(email);
        this.date = date;
    }

    private String validateToken(String token) {
        if (token.isBlank()) {
            throw new IllegalArgumentException(
                    "token cannot be blank" + "[token=" + token + "]"
            );
        }
        return token;
    }

    public long getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }

    public Instant getDate() {
        return date;
    }

}
