package br.com.ale.domain.auth;

import java.time.Instant;

public class AuthToken {

    private final long clientId;
    private final String token;
    private final Instant date;

    public AuthToken(
            long clientId,
            String token,
            Instant date
    ) {
        this.clientId = clientId;
        this.token = validateToken(token);
        this.date = date;
    }

    private String validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "token cannot be blank [token=" + token + "]"
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
