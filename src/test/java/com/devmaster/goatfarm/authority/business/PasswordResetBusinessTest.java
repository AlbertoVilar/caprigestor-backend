package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.*;
import com.devmaster.goatfarm.authority.business.bo.*;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetBusinessTest {
    @Mock UserPersistencePort users; @Mock PasswordResetTokenPersistencePort tokens; @Mock PasswordResetMailPort mail; @Mock RefreshSessionPersistencePort sessions; @Mock PasswordHashingPort hashing;
    PasswordResetBusiness business; Clock clock; AuthorityAccount user;
    @BeforeEach void setUp() { clock=Clock.fixed(Instant.parse("2026-03-28T12:00:00Z"), ZoneOffset.UTC); business=new PasswordResetBusiness(users,tokens,mail,sessions,hashing,30,60,clock); user=new AuthorityAccount(7L,"QA Reset","qa.reset@example.com","12345678901","encoded-old",java.util.Set.of("ROLE_OPERATOR")); }
    @Test void requestPersistsHashedToken() { when(users.findByEmail("qa.reset@example.com")).thenReturn(Optional.of(user)); when(tokens.findLatestByUserId(7L)).thenReturn(Optional.empty()); when(tokens.save(any())).thenAnswer(i->i.getArgument(0)); var response=business.requestPasswordReset(PasswordResetRequestVO.builder().email("QA.Reset@example.com").build()); assertEquals(PasswordResetBusiness.NEUTRAL_MESSAGE,response.getMessage()); var captor=org.mockito.ArgumentCaptor.forClass(PasswordResetTokenRecord.class); verify(tokens).save(captor.capture()); var saved=captor.getValue(); assertEquals(7L,saved.getUserId()); assertEquals(64,saved.getTokenHash().length()); assertEquals(Instant.parse("2026-03-28T12:00:00Z"),saved.getCreatedAt()); verify(mail).sendPasswordResetMail(eq("qa.reset@example.com"),anyString(),eq(Duration.ofMinutes(30))); }
    @Test void missingEmailIsNeutral() { when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty()); assertEquals(PasswordResetBusiness.NEUTRAL_MESSAGE,business.requestPasswordReset(PasswordResetRequestVO.builder().email("missing@example.com").build()).getMessage()); verifyNoInteractions(mail); }
    @Test void cooldownAvoidsDispatch() { var latest=new PasswordResetTokenRecord(null,7L,user.email(),"hash",Instant.parse("2026-03-28T12:30:00Z"),null,null,Instant.parse("2026-03-28T11:59:30Z")); when(users.findByEmail(user.email())).thenReturn(Optional.of(user)); when(tokens.findLatestByUserId(7L)).thenReturn(Optional.of(latest)); business.requestPasswordReset(PasswordResetRequestVO.builder().email(user.email()).build()); verify(tokens,never()).save(any()); verifyNoInteractions(mail); }
    @Test void confirmValidTokenUpdatesPassword() { var token=token(Instant.parse("2026-03-28T12:25:00Z")); when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token)); when(hashing.hash("NovaSenha123")).thenReturn("encoded-new"); when(tokens.save(any())).thenAnswer(i->i.getArgument(0)); assertEquals(PasswordResetBusiness.SUCCESS_MESSAGE,business.confirmPasswordReset(PasswordResetConfirmVO.builder().token("raw-token").newPassword("NovaSenha123").confirmPassword("NovaSenha123").build()).getMessage()); assertNotNull(token.getUsedAt()); verify(users).updatePassword(7L,"encoded-new"); verify(sessions).revokeAllForUser(eq(7L),any(),eq("password_reset")); verify(tokens).revokeActiveTokens(eq(7L),any(),any()); }
    @Test void invalidTokenIsRejected() { when(tokens.findByTokenHash(anyString())).thenReturn(Optional.empty()); assertThrows(InvalidArgumentException.class,()->business.confirmPasswordReset(PasswordResetConfirmVO.builder().token("raw-token").newPassword("NovaSenha123").confirmPassword("NovaSenha123").build())); verifyNoInteractions(hashing); }
    @Test void expiredTokenIsRejected() { when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token(Instant.parse("2026-03-28T11:59:59Z")))); assertThrows(InvalidArgumentException.class,()->business.confirmPasswordReset(PasswordResetConfirmVO.builder().token("raw-token").newPassword("NovaSenha123").confirmPassword("NovaSenha123").build())); }
    @Test void usedTokenIsRejected() { var token=token(Instant.parse("2026-03-28T12:25:00Z")); token.setUsedAt(Instant.now()); when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token)); assertThrows(InvalidArgumentException.class,()->business.confirmPasswordReset(PasswordResetConfirmVO.builder().token("raw-token").newPassword("NovaSenha123").confirmPassword("NovaSenha123").build())); }
    @Test void mismatchedPasswordsAreRejected() { assertThrows(InvalidArgumentException.class,()->business.confirmPasswordReset(PasswordResetConfirmVO.builder().token("raw-token").newPassword("NovaSenha123").confirmPassword("OutraSenha123").build())); verifyNoInteractions(tokens); }
    private PasswordResetTokenRecord token(Instant expires) { return new PasswordResetTokenRecord(11L,7L,user.email(),"hash",expires,null,null,Instant.parse("2026-03-28T11:55:00Z")); }
}
