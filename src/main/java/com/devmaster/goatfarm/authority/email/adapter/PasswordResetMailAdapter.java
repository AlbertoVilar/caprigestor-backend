package com.devmaster.goatfarm.authority.email.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetMailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PasswordResetMailAdapter implements PasswordResetMailPort {

    private final JavaMailSender javaMailSender;
    private final String frontendBaseUrl;
    private final String fromAddress;

    public PasswordResetMailAdapter(JavaMailSender javaMailSender,
                                    @Value("${caprigestor.auth.password-reset.frontend-base-url}") String frontendBaseUrl,
                                    @Value("${caprigestor.auth.password-reset.from-address}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.frontendBaseUrl = sanitizeBaseUrl(frontendBaseUrl);
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendPasswordResetMail(String recipientEmail, String rawToken, Duration ttl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Recuperacao de senha - CapriGestor");
        message.setText(buildMessage(rawToken, ttl));
        javaMailSender.send(message);
    }

    private String buildMessage(String rawToken, Duration ttl) {
        String link = frontendBaseUrl + "/reset-password?token=" + rawToken;
        long ttlMinutes = ttl.toMinutes();
        return "Voce solicitou a redefinicao da sua senha no CapriGestor.\n\n"
                + "Acesse o link abaixo para continuar:\n"
                + link + "\n\n"
                + "Este link e de uso unico e expira em " + ttlMinutes + " minutos.\n"
                + "Se voce nao solicitou essa redefinicao, ignore este email.";
    }

    private String sanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://127.0.0.1:5173";
        }
        String sanitized = baseUrl.trim();
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }
}
