package com.devmaster.goatfarm.authority.business.bo;

import java.time.Instant;

/** Application representation of a password reset token. */
public final class PasswordResetTokenRecord {
    private Long id;
    private Long userId;
    private String userEmail;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant revokedAt;
    private Instant createdAt;

    public PasswordResetTokenRecord() {
    }

    public PasswordResetTokenRecord(Long id, Long userId, String userEmail, String tokenHash,
                                    Instant expiresAt, Instant usedAt, Instant revokedAt,
                                    Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
