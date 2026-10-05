package com.devmaster.goatfarm.authority.persistence.mapper;

import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.AuthorityRole;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetTokenRecord;
import com.devmaster.goatfarm.authority.business.bo.RefreshSessionRecord;
import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class AuthorityPersistenceMapper {

    public AuthorityAccount toAccount(User user) {
        if (user == null) return null;
        return new AuthorityAccount(user.getId(), user.getName(), user.getEmail(), user.getCpf(),
                user.getPassword(), user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toSet()));
    }

    public User toEntity(AuthorityAccount account) {
        User user = new User();
        user.setId(account.id());
        user.setName(account.name());
        user.setEmail(account.email());
        user.setCpf(account.cpf());
        user.setPassword(account.encodedPassword());
        return user;
    }

    public AuthorityRole toRole(Role role) {
        return role == null ? null : new AuthorityRole(role.getAuthority(), role.getDescription());
    }

    public Role toEntity(AuthorityRole role) {
        return role == null ? null : new Role(role.authority(), role.description());
    }

    public RefreshSessionRecord toRefreshSession(RefreshSession session) {
        return new RefreshSessionRecord(session.getId(), toAccount(session.getUser()), session.getTokenHash(),
                session.getTokenId(), session.getFamilyId(), session.getIssuedAt(), session.getExpiresAt(),
                session.getConsumedAt(), session.getRevokedAt());
    }

    public RefreshSession toEntity(RefreshSessionRecord record, User user) {
        return RefreshSession.builder().id(record.getId()).user(user).tokenHash(record.getTokenHash())
                .tokenId(record.getTokenId()).familyId(record.getFamilyId()).issuedAt(record.getIssuedAt())
                .expiresAt(record.getExpiresAt()).consumedAt(record.getConsumedAt()).revokedAt(record.getRevokedAt())
                .build();
    }

    public PasswordResetTokenRecord toPasswordResetToken(PasswordResetToken token) {
        User user = token.getUser();
        return new PasswordResetTokenRecord(token.getId(), user.getId(), user.getEmail(), token.getTokenHash(),
                token.getExpiresAt(), token.getUsedAt(), token.getRevokedAt(), token.getCreatedAt());
    }

    public PasswordResetToken toEntity(PasswordResetTokenRecord record, User user) {
        return PasswordResetToken.builder().id(record.getId()).user(user).tokenHash(record.getTokenHash())
                .expiresAt(record.getExpiresAt()).usedAt(record.getUsedAt()).revokedAt(record.getRevokedAt())
                .createdAt(record.getCreatedAt()).build();
    }
}
