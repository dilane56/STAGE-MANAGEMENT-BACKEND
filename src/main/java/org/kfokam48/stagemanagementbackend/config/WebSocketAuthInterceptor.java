package org.kfokam48.stagemanagementbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.kfokam48.stagemanagementbackend.model.auth.CustomUserDetails;
import org.kfokam48.stagemanagementbackend.service.auth.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentifie la connexion STOMP à partir de l'en-tête "Authorization: Bearer <jeton>" de la trame CONNECT.
 * Le jeton est de préférence un ticket WebSocket (30 s, usage unique, obtenu via POST /api/auth/ws-ticket) ;
 * un access token est encore accepté pour les outils de test.
 * <p>
 * Le Principal de la session porte l'ID de l'utilisateur comme nom : c'est ce qui permet à
 * {@code convertAndSendToUser(userId, "/queue/messages", ...)} de router les messages vers
 * le bon client (abonné à "/user/queue/messages").
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    /** Tickets déjà utilisés (jti → expiration), pour garantir l'usage unique. */
    private final Map<String, Instant> usedTickets = new ConcurrentHashMap<>();

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Jeton d'authentification manquant");
            }
            try {
                String email = resolveEmail(authorization.substring(7));
                CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
                accessor.setUser(new UsernamePasswordAuthenticationToken(
                        String.valueOf(user.getId()), null, user.getAuthorities()));
                logger.debug("Connexion WebSocket authentifiée pour l'utilisateur {}", user.getId());
            } catch (RuntimeException e) {
                logger.warn("Connexion WebSocket refusée : {}", e.getMessage());
                throw new MessageDeliveryException("Jeton d'authentification invalide");
            }
        }

        return message;
    }

    private String resolveEmail(String token) {
        try {
            Claims ticket = jwtService.validate(token, JwtService.TYPE_WS);
            consumeTicket(ticket);
            return ticket.getSubject();
        } catch (JwtException notATicket) {
            return jwtService.validateAndGetSubject(token, JwtService.TYPE_ACCESS);
        }
    }

    private void consumeTicket(Claims ticket) {
        Instant now = Instant.now();
        usedTickets.values().removeIf(expiration -> expiration.isBefore(now));
        if (usedTickets.putIfAbsent(ticket.getId(), ticket.getExpiration().toInstant()) != null) {
            throw new MessageDeliveryException("Ticket WebSocket déjà utilisé");
        }
    }
}
