package org.kfokam48.stagemanagementbackend.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[64]);

    private final JwtService jwtService = new JwtService(SECRET, 60_000, 120_000, 30_000);

    @Test
    void accessTokenIsAcceptedAsAccessToken() {
        String token = jwtService.generateAccessToken("etudiant@test.fr");
        assertEquals("etudiant@test.fr", jwtService.validateAndGetSubject(token, JwtService.TYPE_ACCESS));
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String refreshToken = jwtService.generateRefreshToken("etudiant@test.fr");
        assertThrows(JwtException.class, () -> jwtService.validateAndGetSubject(refreshToken, JwtService.TYPE_ACCESS));
        assertEquals("etudiant@test.fr", jwtService.validateAndGetSubject(refreshToken, JwtService.TYPE_REFRESH));
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        String accessToken = jwtService.generateAccessToken("etudiant@test.fr");
        assertThrows(JwtException.class, () -> jwtService.validateAndGetSubject(accessToken, JwtService.TYPE_REFRESH));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder().encodeToString("une-autre-cle-tres-longue-pour-hmac-sha-256!!".getBytes());
        String token = new JwtService(otherSecret, 60_000, 120_000, 30_000).generateAccessToken("pirate@test.fr");
        assertThrows(JwtException.class, () -> jwtService.validateAndGetSubject(token, JwtService.TYPE_ACCESS));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = new JwtService(SECRET, -1_000, -1_000, -1_000).generateAccessToken("etudiant@test.fr");
        assertThrows(ExpiredJwtException.class, () -> jwtService.validateAndGetSubject(token, JwtService.TYPE_ACCESS));
    }

    @Test
    void tooShortSecretIsRefusedAtStartup() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalStateException.class, () -> new JwtService(shortSecret, 60_000, 120_000, 30_000));
    }
}
