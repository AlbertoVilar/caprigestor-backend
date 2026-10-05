package com.devmaster.goatfarm.authority.application.ports.out;

/**
 * Application boundary for hashing user passwords.
 *
 * <p>The Authority core depends only on this plain-Java contract. The
 * concrete hashing algorithm is supplied by an infrastructure adapter.</p>
 */
public interface PasswordHashingPort {

    String hash(String rawPassword);
}
