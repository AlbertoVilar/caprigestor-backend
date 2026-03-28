package com.devmaster.goatfarm.authority.application.ports.out;

import java.time.Duration;

public interface PasswordResetMailPort {

    void sendPasswordResetMail(String recipientEmail, String rawToken, Duration ttl);
}
