package br.com.ale.infrastructure.auth;

import br.com.ale.domain.auth.AuthToken;
import br.com.ale.domain.auth.TokenClaims;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.service.SignatureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

public class SimpleTokenGenerator implements TokenGenerator {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public SimpleTokenGenerator(
            PrivateKey privateKey,
            PublicKey publicKey
    ) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    public AuthToken generate(long clientId) {

        Instant expiresAt =
                Instant.now().plus(1, ChronoUnit.HOURS);

        Instant issuedAt = Instant.now();
        String tokenId = UUID.randomUUID().toString();

        String payload =
                clientId + ":" + issuedAt.toEpochMilli() + ":" +
                        expiresAt.toEpochMilli() + ":" + tokenId;

        String signature =
                SignatureService.sign(payload, privateKey);

        String token =
                Base64.getEncoder()
                        .encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                        + "." + signature;

        return new AuthToken(
                clientId,
                token,
                expiresAt
        );
    }

    @Override
    public TokenClaims validate(String token) {

        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                throw new InvalidCredentialsException("Invalid token format");
            }

            String payloadJson = new String(
                    Base64.getDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            String signature = parts[2];

            boolean valid = SignatureService.verify(payloadJson, signature, publicKey);

            if (!valid) {
                throw new InvalidCredentialsException("Invalid token signature");
            }

            // Parse JSON do payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payloadJson);

            long clientId = node.get("sub").asLong();
            long expiresAtMillis = node.get("exp").asLong() * 1000;

            Instant expiresAt = Instant.ofEpochMilli(expiresAtMillis);

            if (expiresAt.isBefore(Instant.now())) {
                throw new InvalidCredentialsException("Token expired");
            }

            return new TokenClaims(clientId, expiresAt);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }

    public String signForTest(String payload) {
        return SignatureService.sign(payload, privateKey);
    }
}
