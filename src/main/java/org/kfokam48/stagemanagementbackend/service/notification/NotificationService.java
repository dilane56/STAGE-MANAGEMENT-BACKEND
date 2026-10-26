package org.kfokam48.stagemanagementbackend.service.notification;

import lombok.RequiredArgsConstructor;

import org.kfokam48.stagemanagementbackend.dto.notification.NotificationResponseDTO;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.mapper.NotificationMapper;
import org.kfokam48.stagemanagementbackend.model.Notification;
import org.kfokam48.stagemanagementbackend.model.Utilisateur;
import org.kfokam48.stagemanagementbackend.repository.NotificationRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.kfokam48.stagemanagementbackend.service.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUserService currentUserService;


    public Notification createnotifcation(Long recepteurId, String titre, String message, boolean sendMail ){
        Notification notif = Notification.builder()
                .destinataireId(recepteurId)
                .titre(titre)
                .dateEnvoi(LocalDateTime.now())
                .message(message)
                .lu(false)
                .build();

        notificationRepository.save(notif);

        // Envoi d'email (un échec SMTP ne doit pas faire échouer l'action métier)
        if (sendMail) {
            Utilisateur user = utilisateurRepository.findById(recepteurId).orElse(null);
            String email = user != null ? user.getEmail() : null;
            if (email != null && !email.isEmpty()) {
                try {
                    emailService.sendEmail(email, titre, message);
                } catch (RuntimeException e) {
                    logger.warn("Échec de l'envoi de l'email de notification à {} : {}", email, e.getMessage());
                }
            }
        }

        return notif;
    }

    /**
     * Enregistre une notification et la pousse en temps réel (STOMP : /user/queue/notifications).
     * Remplace l'ancien NotificationController.sendNotification appelé directement par les services.
     */
    public void sendNotification(Long recepteurId, String title, String message, boolean sendMail) {
        Notification notif = createnotifcation(recepteurId, title, message, sendMail);
        try {
            messagingTemplate.convertAndSendToUser(recepteurId.toString(), "/queue/notifications",
                    convertToNotificationResponseDTO(notif));
        } catch (RuntimeException e) {
            logger.warn("Échec de l'envoi temps réel de la notification {} : {}", notif.getId(), e.getMessage());
        }
    }

    /** Notifie tous les administrateurs (remplace l'ID administrateur 1 codé en dur). */
    public void notifyAdmins(String title, String message) {
        utilisateurRepository.findByRole(Roles.ADMIN)
                .forEach(admin -> sendNotification(admin.getId(), title, message, false));
    }

    public NotificationResponseDTO convertToNotificationResponseDTO(Notification notification){
        return notificationMapper.notficationToNotificationResponseDTO(notification);
    }

    public List<NotificationResponseDTO> getUserNotifications(Long recepteurId) {
        currentUserService.checkSelf(recepteurId);
        return  notificationMapper.notificationListToNotificationResponseDTOList(notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(recepteurId));
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.getDestinataireId().equals(currentUserService.getCurrentUserId())) {
                throw new AccessDeniedException("Cette notification ne vous appartient pas");
            }
            n.setLu(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsReadByUserId(Long userId) {
        currentUserService.checkSelf(userId);
        List<Notification> notifications = notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(userId);
        notifications.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(notifications);
    }

    public int getNumberOfNotReadNotificationByUserId(Long userId){
        currentUserService.checkSelf(userId);
        return (int) notificationRepository.countByDestinataireIdAndLuFalse(userId);
    }
}
