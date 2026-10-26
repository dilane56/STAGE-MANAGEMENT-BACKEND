package org.kfokam48.stagemanagementbackend.controlleur.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.kfokam48.stagemanagementbackend.config.AuthCookieService;
import org.kfokam48.stagemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.stagemanagementbackend.exception.InvalidTokenException;
import org.kfokam48.stagemanagementbackend.service.auth.AuthService;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentification par cookies HttpOnly : les jetons ne sont plus renvoyés dans le corps JSON
 * (sauf si app.auth.tokens-in-body=true, pratique pour tester avec Swagger/Postman en local).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final CurrentUserService currentUserService;
    private final boolean tokensInBody;

    public AuthController(AuthService authService, AuthCookieService authCookieService,
                          CurrentUserService currentUserService,
                          @Value("${app.auth.tokens-in-body:false}") boolean tokensInBody) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.currentUserService = currentUserService;
        this.tokensInBody = tokensInBody;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(@Valid @RequestBody LoginRequest authRequest,
                                                                HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(authRequest);
        authCookieService.writeAccessCookie(response, result.accessToken());
        authCookieService.writeRefreshCookie(response, result.refreshToken());

        Map<String, Object> data = new HashMap<>();
        data.put("user", result.user());
        if (tokensInBody) {
            data.put("token", result.accessToken());
            data.put("refreshToken", result.refreshToken());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    /**
     * Le refresh token est lu dans son cookie ; le champ JSON "refreshToken" reste accepté
     * pour les outils de test.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody(required = false) Map<String, String> request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse response) {
        String refreshToken = authCookieService.read(httpRequest, AuthCookieService.REFRESH_COOKIE)
                .orElse(request != null ? request.get("refreshToken") : null);
        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            authCookieService.writeAccessCookie(response, newAccessToken);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            if (tokensInBody) {
                body.put("token", newAccessToken);
            }
            return ResponseEntity.ok(body);
        } catch (InvalidTokenException e) {
            authCookieService.clear(response);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "REFRESH_TOKEN_EXPIRED");
            errorResponse.put("message", "Session expirée, reconnexion requise");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /** Supprime les cookies d'authentification. */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        authCookieService.clear(response);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(body);
    }

    /**
     * Ticket WebSocket : le chat se connecte directement au backend (le proxy Vercel ne relaie pas
     * les WebSockets), il ne reçoit donc pas le cookie. Il demande ce ticket de 30 secondes, à usage
     * unique, et l'envoie dans la trame STOMP CONNECT.
     */
    @PostMapping("/ws-ticket")
    public ResponseEntity<Map<String, String>> webSocketTicket() {
        String email = currentUserService.getCurrentUser().getUsername();
        return ResponseEntity.ok(Map.of("ticket", authService.createWebSocketTicket(email)));
    }
}
