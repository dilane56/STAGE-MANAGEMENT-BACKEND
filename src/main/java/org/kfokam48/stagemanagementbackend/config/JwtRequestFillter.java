package org.kfokam48.stagemanagementbackend.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Authentifie la requête à partir :
 * <ol>
 *     <li>de l'en-tête "Authorization: Bearer ..." (Swagger, outils de test) ;</li>
 *     <li>sinon du cookie HttpOnly "access_token" (application web).</li>
 * </ol>
 * Protection CSRF : une requête authentifiée par cookie qui modifie des données (POST, PUT, PATCH, DELETE)
 * doit porter l'en-tête "X-Requested-With". Un site tiers ne peut pas ajouter cet en-tête sans
 * autorisation CORS ; combiné à SameSite=Lax, cela empêche les requêtes forgées.
 */
@Component
public class JwtRequestFillter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFillter.class);

    public static final String CSRF_HEADER = "X-Requested-With";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of("/api/auth/login", "/api/auth/refresh", "/api/auth/logout");

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthCookieService authCookieService;

    public JwtRequestFillter(JwtService jwtService, CustomUserDetailsService userDetailsService,
                             AuthCookieService authCookieService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authCookieService = authCookieService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Endpoints publics d'authentification : aucun jeton à analyser
        return PUBLIC_AUTH_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authorizationHeader = request.getHeader("Authorization");
            boolean fromCookie = false;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
            } else {
                Optional<String> cookie = authCookieService.read(request, AuthCookieService.ACCESS_COOKIE);
                if (cookie.isPresent()) {
                    jwt = cookie.get();
                    fromCookie = true;
                }
            }

            if (jwt != null) {
                if (fromCookie && !SAFE_METHODS.contains(request.getMethod()) && request.getHeader(CSRF_HEADER) == null) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN, "CSRF_HEADER_MISSING",
                            "En-tête " + CSRF_HEADER + " requis.");
                    return;
                }
                try {
                    // Seuls les access tokens sont acceptés ici (un refresh token est rejeté)
                    String userEmail = jwtService.validateAndGetSubject(jwt, JwtService.TYPE_ACCESS);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (ExpiredJwtException e) {
                    logger.debug("Token JWT expiré : {}", e.getMessage());
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "Token expiré.");
                    return;
                } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                    logger.warn("Token JWT invalide : {}", e.getMessage());
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALID", "Token invalide.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String code, String message) throws IOException {
        if (!response.isCommitted()) {
            response.resetBuffer();
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + code + "\",\"message\":\"" + message + "\"}");
        }
    }
}
