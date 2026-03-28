package br.com.ale.domain.emailVerification;

import java.time.Instant;

public class EmailVerification {

    private Long id;
    private Long clientId;
    private String token;
    private EmailVerificationType type;
    private Instant expiresAt;
    private Instant verifiedAt;
    private Instant createdAt;

    public EmailVerification(Long id, Long clientId, String token, EmailVerificationType type,
                             Instant expiresAt, Instant verifiedAt, Instant createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.token = token;
        this.type = type;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    public EmailVerificationType getType() {
        return type;
    }

    public void setType(EmailVerificationType type) {
        this.type = type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }
}