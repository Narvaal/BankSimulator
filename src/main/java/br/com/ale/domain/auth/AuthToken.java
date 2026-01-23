package br.com.ale.domain.auth;

import java.time.Instant;

public class AuthToken {
    private final long clientID;
    private final String token;
    private final Instant date;


    public AuthToken(
            long clientID,
            String document,
            Instant date
    ) {
        this.clientID = clientID;
        this.token = validateToken(document);
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

    public long getClientID() {
        return clientID;
    }

    public String getToken() {
        return token;
    }

    public Instant getDate() {
        return date;
    }

}
