package org.kfokam48.stagemanagementbackend.config;

import lombok.RequiredArgsConstructor;

import org.kfokam48.stagemanagementbackend.enums.UserStatus;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;


/**
 * Met à jour le statut EN_LIGNE / HORS_LIGNE des utilisateurs à partir du Principal
 * de la session WebSocket (positionné par WebSocketAuthInterceptor).
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final UtilisateurRepository utilisateurRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        updateStatus(event, UserStatus.EN_LIGNE);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        updateStatus(event, UserStatus.HORS_LIGNE);
    }

    private void updateStatus(AbstractSubProtocolEvent event, UserStatus status) {
        Principal principal = event.getUser();
        if (principal == null) {
            return;
        }
        try {
            Long userId = Long.parseLong(principal.getName());
            utilisateurRepository.findById(userId).ifPresent(user -> {
                Instant now = Instant.now();
                user.setStatus(status);
                user.setDerniereConnexion(now);
                utilisateurRepository.save(user);
                messagingTemplate.convertAndSend("/topic/status", Map.of(
                        "userId", user.getId(),
                        "status", status.name(),
                        "derniereConnexion", now.toString()));
            });
        } catch (NumberFormatException e) {
            logger.warn("Principal WebSocket inattendu : {}", principal.getName());
        }
    }
}
