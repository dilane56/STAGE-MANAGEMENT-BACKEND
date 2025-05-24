package org.kfokam48.stagemanagementbackend.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.kfokam48.stagemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.exception.AuthenticationFailedException;
import org.kfokam48.stagemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Date;

@Service
public class AuthService {



    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UtilisateurRepository utilisateurRepository ;
    public static final SecretKey secretKey = Keys.hmacShaKeyFor(generateSecureKey(256).getEncoded());

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, UtilisateurRepository utilisateurRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.utilisateurRepository = utilisateurRepository;
    }

    public String authenticateUser(@Valid LoginRequest authRequest) {
        try {
            // Authentification
            System.out.println("Authentification en cours...");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            // Récupération des détails de l'utilisateur
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            System.out.println("Authentification réussie pour l'utilisateur : " + userDetails.getUsername());
//            // Génération du token JWT
            return Jwts.builder()
                    .issuer("CLINIQUE-MANAGEMENT")
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // Expire dans 1 jour
                    .signWith(secretKey)
                    .compact();

        } catch (Exception e) {
            // Gestion des erreurs avec un message explicite
            System.out.println("Erreur d'authentification : " + e.getMessage());
            throw new AuthenticationFailedException("Identifiants invalides : vérifiez l'e-mail ou le mot de passe.");
        }

    }
    public Roles getUserRole(LoginRequest loginRequest){
        Utilisateur user = utilisateurRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new RessourceNotFoundException("user not found"));
        return user.getRole();
    }

    // Generation de la clé de sécurité
    public static SecretKey generateSecureKey(int keySize) {
        byte[] keyBytes = new byte[keySize / 8]; // Convertir bits en bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes); // Générer des bytes aléatoires
        return new SecretKeySpec(keyBytes, "HmacSHA256"); // Adapter selon l'algorithme souhaité
    }

}