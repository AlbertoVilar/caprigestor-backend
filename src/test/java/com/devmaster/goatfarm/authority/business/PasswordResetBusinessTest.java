package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetMailPort;
import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetTokenPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetConfirmVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetRequestVO;
import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetBusinessTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordResetTokenPersistencePort passwordResetTokenPersistencePort;

    @Mock
    private PasswordResetMailPort passwordResetMailPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetBusiness passwordResetBusiness;
    private Clock clock;
    private User user;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-03-28T12:00:00Z"), ZoneOffset.UTC);
        passwordResetBusiness = new PasswordResetBusiness(
                userPersistencePort,
                passwordResetTokenPersistencePort,
                passwordResetMailPort,
                passwordEncoder,
                30,
                60,
                clock
        );

        user = new User();
        user.setId(7L);
        user.setEmail("qa.reset@example.com");
        user.setPassword("encoded-old");
        user.setName("QA Reset");
        user.setCpf("12345678901");
    }

    @Test
    void shouldReturnNeutralResponseAndPersistHashedTokenWhenEmailExists() {
        when(userPersistencePort.findByEmail("qa.reset@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenPersistencePort.findLatestByUserId(7L)).thenReturn(Optional.empty());
        when(passwordResetTokenPersistencePort.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = passwordResetBusiness.requestPasswordReset(PasswordResetRequestVO.builder()
                .email("QA.Reset@example.com")
                .build());

        assertEquals(PasswordResetBusiness.NEUTRAL_MESSAGE, response.getMessage());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenPersistencePort).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertEquals(64, savedToken.getTokenHash().length());
        assertEquals(Instant.parse("2026-03-28T12:00:00Z"), savedToken.getCreatedAt());
        assertEquals(Instant.parse("2026-03-28T12:30:00Z"), savedToken.getExpiresAt());

        verify(passwordResetTokenPersistencePort).revokeActiveTokens(eq(7L), eq(Instant.parse("2026-03-28T12:00:00Z")), eq(Instant.parse("2026-03-28T12:00:00Z")));
        verify(passwordResetMailPort).sendPasswordResetMail(eq("qa.reset@example.com"), any(String.class), eq(Duration.ofMinutes(30)));
    }

    @Test
    void shouldReturnNeutralResponseWhenEmailDoesNotExist() {
        when(userPersistencePort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        var response = passwordResetBusiness.requestPasswordReset(PasswordResetRequestVO.builder()
                .email("missing@example.com")
                .build());

        assertEquals(PasswordResetBusiness.NEUTRAL_MESSAGE, response.getMessage());
        verify(passwordResetTokenPersistencePort, never()).save(any());
        verify(passwordResetMailPort, never()).sendPasswordResetMail(anyString(), anyString(), any());
    }

    @Test
    void shouldRespectCooldownAndAvoidNewEmailDispatch() {
        PasswordResetToken latestToken = PasswordResetToken.builder()
                .createdAt(Instant.parse("2026-03-28T11:59:30Z"))
                .build();

        when(userPersistencePort.findByEmail("qa.reset@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenPersistencePort.findLatestByUserId(7L)).thenReturn(Optional.of(latestToken));

        var response = passwordResetBusiness.requestPasswordReset(PasswordResetRequestVO.builder()
                .email("qa.reset@example.com")
                .build());

        assertEquals(PasswordResetBusiness.NEUTRAL_MESSAGE, response.getMessage());
        verify(passwordResetTokenPersistencePort, never()).revokeActiveTokens(anyLong(), any(), any());
        verify(passwordResetTokenPersistencePort, never()).save(any());
        verify(passwordResetMailPort, never()).sendPasswordResetMail(anyString(), anyString(), any());
    }

    @Test
    void shouldConfirmValidTokenAndUpdatePassword() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(11L)
                .user(user)
                .tokenHash("hash")
                .createdAt(Instant.parse("2026-03-28T11:55:00Z"))
                .expiresAt(Instant.parse("2026-03-28T12:25:00Z"))
                .build();

        when(passwordResetTokenPersistencePort.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("encoded-new");
        when(passwordResetTokenPersistencePort.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = passwordResetBusiness.confirmPasswordReset(PasswordResetConfirmVO.builder()
                .token("raw-token")
                .newPassword("NovaSenha123")
                .confirmPassword("NovaSenha123")
                .build());

        assertEquals(PasswordResetBusiness.SUCCESS_MESSAGE, response.getMessage());
        assertEquals(Instant.parse("2026-03-28T12:00:00Z"), token.getUsedAt());
        verify(userPersistencePort).updatePassword(7L, "encoded-new");
        verify(passwordResetTokenPersistencePort).save(token);
        verify(passwordResetTokenPersistencePort).revokeActiveTokens(eq(7L), eq(Instant.parse("2026-03-28T12:00:00Z")), eq(Instant.parse("2026-03-28T12:00:00Z")));
    }

    @Test
    void shouldRejectInvalidToken() {
        when(passwordResetTokenPersistencePort.findByTokenHash(anyString())).thenReturn(Optional.empty());

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                passwordResetBusiness.confirmPasswordReset(PasswordResetConfirmVO.builder()
                        .token("raw-token")
                        .newPassword("NovaSenha123")
                        .confirmPassword("NovaSenha123")
                        .build())
        );

        assertEquals("Token de redefinicao invalido.", exception.getMessage());
        verify(userPersistencePort, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void shouldRejectExpiredToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash("hash")
                .createdAt(Instant.parse("2026-03-28T11:00:00Z"))
                .expiresAt(Instant.parse("2026-03-28T11:59:59Z"))
                .build();
        when(passwordResetTokenPersistencePort.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                passwordResetBusiness.confirmPasswordReset(PasswordResetConfirmVO.builder()
                        .token("raw-token")
                        .newPassword("NovaSenha123")
                        .confirmPassword("NovaSenha123")
                        .build())
        );

        assertEquals("Token de redefinicao expirado.", exception.getMessage());
    }

    @Test
    void shouldRejectUsedToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash("hash")
                .createdAt(Instant.parse("2026-03-28T11:55:00Z"))
                .expiresAt(Instant.parse("2026-03-28T12:25:00Z"))
                .usedAt(Instant.parse("2026-03-28T11:58:00Z"))
                .build();
        when(passwordResetTokenPersistencePort.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                passwordResetBusiness.confirmPasswordReset(PasswordResetConfirmVO.builder()
                        .token("raw-token")
                        .newPassword("NovaSenha123")
                        .confirmPassword("NovaSenha123")
                        .build())
        );

        assertEquals("Token de redefinicao ja foi utilizado.", exception.getMessage());
    }

    @Test
    void shouldRejectWhenPasswordsDoNotMatch() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () ->
                passwordResetBusiness.confirmPasswordReset(PasswordResetConfirmVO.builder()
                        .token("raw-token")
                        .newPassword("NovaSenha123")
                        .confirmPassword("OutraSenha123")
                        .build())
        );

        assertEquals("Confirmacao de senha nao confere.", exception.getMessage());
        verify(passwordResetTokenPersistencePort, never()).findByTokenHash(anyString());
    }
}
