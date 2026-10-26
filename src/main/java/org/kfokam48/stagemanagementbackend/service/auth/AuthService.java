package org.kfokam48.stagemanagementbackend.service.auth;

import org.kfokam48.stagemanagementbackend.config.JwtService;
import org.kfokam48.stagemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.stagemanagementbackend.exception.AuthenticationFailedException;
import org.kfokam48.stagemanagementbackend.exception.InvalidTokenException;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    /** Résultat d'une connexion : profil public de l'utilisateur et jetons (posés en cookies par le contrôleur). */
    public record LoginResult(Map<String, Object> user, String accessToken, String refreshToken) {
    }

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                       UtilisateurRepository utilisateurRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResult login(LoginRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Identifiants invalides");
        }

        Utilisateur user = utilisateurRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Identifiants invalides"));
        user.setDerniereConnexion(Instant.now());
        utilisateurRepository.save(user);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("name", user.getFullName());
        userMap.put("role", user.getRole().name().toLowerCase());
        userMap.put("avatar", user.getAvatar());
        userMap.put("telephone", user.getTelephone());
        userMap.put("createAt", user.getCreateAt());
        userMap.put("updateAt", user.getUpdateAt());
        userMap.put("profile", user.getProfile() != null ? user.getProfile() : new HashMap<>());

        return new LoginResult(userMap,
                jwtService.generateAccessToken(user.getEmail()),
                jwtService.generateRefreshToken(user.getEmail()));
    }

    /** Génère un nouvel access token à partir d'un refresh token valide. */
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token manquant");
        }
        try {
            String username = jwtService.validateAndGetSubject(refreshToken, JwtService.TYPE_REFRESH);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtService.generateAccessToken(userDetails.getUsername());
        } catch (RuntimeException e) {
            throw new InvalidTokenException("Refresh token invalide ou expiré", e);
        }
    }

    /** Ticket de 30 secondes, à usage unique, pour ouvrir la connexion WebSocket. */
    public String createWebSocketTicket(String email) {
        return jwtService.generateWebSocketTicket(email);
    }
}
