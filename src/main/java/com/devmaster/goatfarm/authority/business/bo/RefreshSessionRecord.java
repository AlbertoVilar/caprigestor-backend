package com.devmaster.goatfarm.authority.business.bo;

import java.time.Instant;
import java.util.UUID;

/**
 * Application representation of a refresh session. The JPA entity remains
 * confined to the persistence adapter.
 */
public final class RefreshSessionRecord {
    private Long id;
    private AuthorityAccount user;
    private String tokenHash;
    private UUID tokenId;
    private UUID familyId;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant consumedAt;
    private Instant revokedAt;

    public RefreshSessionRecord() {
    }

    public RefreshSessionRecord(Long id, AuthorityAccount user, String tokenHash, UUID tokenId,
                                UUID familyId, Instant issuedAt, Instant expiresAt,
                                Instant consumedAt, Instant revokedAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.tokenId = tokenId;
        this.familyId = familyId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
        this.revokedAt = revokedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuthorityAccount getUser() { return user; }
    public void setUser(AuthorityAccount user) { this.user = user; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public UUID getTokenId() { return tokenId; }
    public void setTokenId(UUID tokenId) { this.tokenId = tokenId; }
    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID familyId) { this.familyId = familyId; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
