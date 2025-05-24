package org.kfokam48.stagemanagementbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.kfokam48.stagemanagementbackend.service.auth.AuthService;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

@Component
public class JwtRequestFillter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    public JwtRequestFillter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            String jwt = null;
            String userEmail = null;

            String path = request.getRequestURI();
//            if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")||path.equals("/api/v1/auth/login")) {
//                filterChain.doFilter(request, response); // Laisser passer les requêtes Swagger
//                return;
//            }

            // Vérification du header d'autorisation
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                try {
                    SecretKey key = AuthService.secretKey;
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(jwt)
                            .getPayload();
                    userEmail = claims.getSubject();
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("JWT invalide : " + e.getMessage());
                    return;
                }
            }
//            if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Access token required");
//                return;
//            }

            // Configuration du contexte de sécurité
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Une erreur s'est produite : " + e.getMessage());
        }
    }
}



