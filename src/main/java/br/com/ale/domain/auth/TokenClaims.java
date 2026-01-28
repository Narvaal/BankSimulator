package br.com.ale.domain.auth;

import java.time.Instant;

public record TokenClaims(
        long clientId,
        Instant expiresAt
) {}
