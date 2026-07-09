package com.makeyourjurney.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-characters-long", 15);

    @Test
    void generateAccessToken_thenExtractUserId_returnsSameUserId() {
        String token = jwtService.generateAccessToken("user-1", "user@example.com");

        assertThat(jwtService.extractUserId(token)).isEqualTo("user-1");
    }

    @Test
    void isValid_forGeneratedToken_returnsTrue() {
        String token = jwtService.generateAccessToken("user-1", "user@example.com");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_forTamperedToken_returnsFalse() {
        String token = jwtService.generateAccessToken("user-1", "user@example.com");

        assertThat(jwtService.isValid(token + "tampered")).isFalse();
    }
}
