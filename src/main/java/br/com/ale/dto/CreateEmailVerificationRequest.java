package br.com.ale.dto;

import java.time.Instant;

public record CreateEmailVerificationRequest(
        Long id,
        Long clientId,
        String token,
        Instant expiresAt,
        Instant verifiedAt
) {
}
