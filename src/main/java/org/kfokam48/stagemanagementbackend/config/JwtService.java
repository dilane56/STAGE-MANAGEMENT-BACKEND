package org.kfokam48.stagemanagementbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Centralise la génération et la validation des jetons JWT.
 * Chaque jeton porte un claim "type" (access / refresh / ws) afin qu'un jeton ne puisse
 * jamais être utilisé à la place d'un autre.
 */
@Service
public class JwtService {

    public static final String ISSUER = "STAGE-MANAGEMENT";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    /** Ticket à usage unique et de très courte durée pour ouvrir la connexion WebSocket. */
    public static final String TYPE_WS = "ws";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final long wsTicketExpirationMs;

    public JwtService(@Value("${jwt.secret}") String jwtSecret,
                      @Value("${jwt.access-token.expiration-ms:900000}") long accessTokenExpirationMs,
                      @Value("${jwt.refresh-token.expiration-ms:604800000}") long refreshTokenExpirationMs,
                      @Value("${jwt.ws-ticket.expiration-ms:30000}") long wsTicketExpirationMs) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (RuntimeException e) {
            throw new IllegalStateException("'jwt.secret' doit être une chaîne Base64 valide.", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("'jwt.secret' doit faire au moins 256 bits (32 octets) une fois décodé.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.wsTicketExpirationMs = wsTicketExpirationMs;
    }

    public String generateAccessToken(String subject) {
        return generate(subject, TYPE_ACCESS, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String subject) {
        return generate(subject, TYPE_REFRESH, refreshTokenExpirationMs);
    }

    public String generateWebSocketTicket(String subject) {
        return generate(subject, TYPE_WS, wsTicketExpirationMs);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    /**
     * Valide la signature, l'expiration et le type du jeton.
     *
     * @return l'email (subject) du jeton
     * @throws JwtException si le jeton est invalide, expiré ou du mauvais type
     */
    public String validateAndGetSubject(String token, String expectedType) {
        return validate(token, expectedType).getSubject();
    }

    /** Comme {@link #validateAndGetSubject} mais renvoie toutes les claims (ex : l'identifiant "jti"). */
    public Claims validate(String token, String expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Type de jeton invalide");
        }
        return claims;
    }

    private String generate(String subject, String type, long ttlMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject(subject)
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(signingKey)
                .compact();
    }
}
