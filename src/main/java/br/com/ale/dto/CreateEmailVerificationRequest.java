package br.com.ale.dto;

import br.com.ale.domain.emailVerification.EmailVerificationType;

import java.time.Instant;

public record CreateEmailVerificationRequest(
        Long clientId,
        String token,
        EmailVerificationType type,
        Instant expiresAt,
        Instant verifiedAt
) {
}
