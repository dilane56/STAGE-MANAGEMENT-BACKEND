package org.kfokam48.stagemanagementbackend.chat;

import lombok.RequiredArgsConstructor;
import org.kfokam48.stagemanagementbackend.chat.message.MessageDTO;
import org.kfokam48.stagemanagementbackend.chat.message.MessageResponseDTO;
import org.kfokam48.stagemanagementbackend.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO chatMessage, Principal principal) {
        if (principal == null) {
            return;
        }
        String senderId = principal.getName();
        try {
            // L'expéditeur est toujours l'utilisateur authentifié : on ignore la valeur envoyée par le client
            chatMessage.setExpediteurId(Long.parseLong(senderId));

            Message savedMessage = chatService.sendMessages(chatMessage);
            MessageResponseDTO messageResponse = chatService.convertToResponseDTO(savedMessage);

            // Envoi au destinataire
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getDestinataireId().toString(),
                    "/queue/messages",
                    messageResponse
            );
            notificationService.sendNotification(savedMessage.getDestinataire().getId(), "Nouveau message",
                    "Vous avez un nouveau message de " + savedMessage.getExpediteur().getFullName(), false);

            // Envoi à l'expéditeur pour confirmation
            messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", messageResponse);

        } catch (Exception e) {
            logger.warn("Échec de l'envoi d'un message de chat : {}", e.getMessage());
            messagingTemplate.convertAndSendToUser(senderId, "/queue/errors",
                    "Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    /**
     * Conservé pour compatibilité avec les anciens clients : le statut EN_LIGNE est désormais
     * positionné automatiquement à la connexion (WebSocketEventListener).
     */
    @MessageMapping("/chat.join")
    public void addUser(Principal principal) {
        logger.debug("chat.join reçu de l'utilisateur {}", principal != null ? principal.getName() : "anonyme");
    }
}
