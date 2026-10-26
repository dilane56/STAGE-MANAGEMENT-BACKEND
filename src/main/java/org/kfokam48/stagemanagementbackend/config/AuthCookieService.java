package org.kfokam48.stagemanagementbackend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Jetons d'authentification transportés dans des cookies HttpOnly (inaccessibles au JavaScript,
 * donc protégés contre le vol par XSS), au lieu du localStorage.
 * <p>
 * Aucun attribut "Domain" n'est posé : le cookie appartient à l'hôte qui répond. Derrière le proxy
 * Vercel (rewrites), c'est donc le domaine du frontend, quel qu'il soit : aucune configuration
 * de domaine n'est nécessaire et le cookie reste « même site » (pas de blocage des cookies tiers).
 */
@Service
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private static final String ACCESS_PATH = "/api";
    // Le refresh token n'est envoyé qu'aux endpoints d'authentification
    private static final String REFRESH_PATH = "/api/auth";

    private final JwtService jwtService;
    private final boolean secure;
    private final String sameSite;

    public AuthCookieService(JwtService jwtService,
                             @Value("${app.cookies.secure:true}") boolean secure,
                             @Value("${app.cookies.same-site:Lax}") String sameSite) {
        this.jwtService = jwtService;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void writeAccessCookie(HttpServletResponse response, String accessToken) {
        add(response, ACCESS_COOKIE, accessToken, ACCESS_PATH, Duration.ofMillis(jwtService.getAccessTokenExpirationMs()));
    }

    public void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        add(response, REFRESH_COOKIE, refreshToken, REFRESH_PATH, Duration.ofMillis(jwtService.getRefreshTokenExpirationMs()));
    }

    public void clear(HttpServletResponse response) {
        add(response, ACCESS_COOKIE, "", ACCESS_PATH, Duration.ZERO);
        add(response, REFRESH_COOKIE, "", REFRESH_PATH, Duration.ZERO);
    }

    public Optional<String> read(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private void add(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
