package com.devmaster.goatfarm.authority.persistence.repository;

import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from RefreshSession session join fetch session.user where session.tokenHash = :tokenHash")
    Optional<RefreshSession> findByTokenHashForUpdate(String tokenHash);

    @Modifying
    @Query("""
            update RefreshSession session
               set session.consumedAt = :consumedAt
             where session.id = :sessionId
               and session.consumedAt is null
               and session.revokedAt is null
               and session.expiresAt > :consumedAt
            """)
    int consumeIfActive(Long sessionId, Instant consumedAt);

    @Modifying
    @Query("""
            update RefreshSession session
               set session.replacedById = :replacementId
             where session.id = :sessionId
            """)
    void setReplacement(Long sessionId, Long replacementId);

    @Modifying
    @Query("""
            update RefreshSession session
               set session.revokedAt = :revokedAt,
                   session.revokeReason = :reason
             where session.familyId = :familyId
               and session.revokedAt is null
            """)
    void revokeFamily(UUID familyId, Instant revokedAt, String reason);

    @Modifying
    @Query("""
            update RefreshSession session
               set session.revokedAt = :revokedAt,
                   session.revokeReason = :reason
             where session.user.id = :userId
               and session.revokedAt is null
            """)
    void revokeAllForUser(Long userId, Instant revokedAt, String reason);
}
